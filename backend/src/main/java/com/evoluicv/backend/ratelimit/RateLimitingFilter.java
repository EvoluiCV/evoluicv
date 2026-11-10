package com.evoluicv.backend.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private static final String LIMITED_PATH = "/api/cv/analyze";
    private static final int MAX_TRACKED_CLIENTS = 10_000;

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (!shouldLimit(request)) {
            chain.doFilter(request, response);
            return;
        }

        String clientKey = ClientIp.from(request);
        Bucket bucket = bucketFor(clientKey);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000L);
        log.warn("Rate limit excedido para {} em {}", clientKey, LIMITED_PATH);
        writeTooManyRequests(response, retryAfterSeconds);
    }

    private boolean shouldLimit(HttpServletRequest request) {
        return HttpMethod.POST.matches(request.getMethod())
                && LIMITED_PATH.equals(request.getRequestURI());
    }

    private Bucket bucketFor(String key) {
        if (buckets.size() > MAX_TRACKED_CLIENTS) {
            buckets.clear();
        }
        return buckets.computeIfAbsent(key, k -> newBucket());
    }

    private Bucket newBucket() {
        Bandwidth perMinute = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofMinutes(1))
                .build();
        Bandwidth perHour = Bandwidth.builder()
                .capacity(30)
                .refillGreedy(30, Duration.ofHours(1))
                .build();
        return Bucket.builder()
                .addLimit(perMinute)
                .addLimit(perHour)
                .build();
    }

    private void writeTooManyRequests(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));

        String payload = """
                {"timestamp":"%s","status":429,"error":"rate_limit_exceeded",\
                "message":"Muitas requisições. Tente novamente em %d segundo(s)."}\
                """.formatted(Instant.now(), retryAfterSeconds);
        response.getWriter().write(payload);
    }
}
