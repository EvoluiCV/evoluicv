package com.evoluicv.backend.quota;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Cota diária de análises, por IP e global, zerada à meia-noite do fuso configurado.
 * Fica em memória: um restart zera a contagem, o que é aceitável enquanto há uma réplica só.
 */
@Component
@EnableConfigurationProperties(QuotaProperties.class)
public class DailyQuota {

    private final Clock clock;
    private final ZoneId zone;
    private final int perIpDaily;
    private final int globalDaily;
    private final Counter rejectedByIp;
    private final Counter rejectedGlobal;

    private LocalDate day;
    private int usedGlobal;
    private final Map<String, Integer> usedByIp = new HashMap<>();

    public DailyQuota(Clock clock, QuotaProperties props, MeterRegistry registry) {
        this.clock = clock;
        this.zone = props.zone();
        this.perIpDaily = props.perIpDaily();
        this.globalDaily = props.globalDaily();
        this.rejectedByIp = rejected(registry, "ip");
        this.rejectedGlobal = rejected(registry, "global");
    }

    /** Cota por IP 0 desliga o limite por IP (staging). Reserva uma análise para o cliente ou lança {@link DailyQuotaExceededException}. */
    public synchronized void acquire(String clientKey) {
        ZonedDateTime now = ZonedDateTime.now(clock).withZoneSameInstant(zone);
        LocalDate today = now.toLocalDate();
        if (!today.equals(day)) {
            day = today;
            usedByIp.clear();
            usedGlobal = 0;
        }

        int used = usedByIp.getOrDefault(clientKey, 0);
        boolean perIpExhausted = perIpDaily > 0 && used >= perIpDaily;
        if (perIpExhausted || usedGlobal >= globalDaily) {
            (perIpExhausted ? rejectedByIp : rejectedGlobal).increment();
            throw new DailyQuotaExceededException(secondsUntilMidnight(now));
        }
        usedByIp.put(clientKey, used + 1);
        usedGlobal++;
    }

    /**
     * Devolve uma análise reservada hoje, quando a falha não foi do cliente (provedor fora, resposta inválida).
     * Reserva de um dia anterior já foi zerada à meia-noite e não é devolvida.
     */
    public synchronized void release(String clientKey) {
        LocalDate today = ZonedDateTime.now(clock).withZoneSameInstant(zone).toLocalDate();
        if (!today.equals(day)) {
            return;
        }
        usedByIp.computeIfPresent(clientKey, (k, used) -> used > 1 ? used - 1 : null);
        usedGlobal = Math.max(0, usedGlobal - 1);
    }

    private static Counter rejected(MeterRegistry registry, String scope) {
        return Counter.builder("evoluicv.quota.rejected")
                .description("Análises barradas pela cota diária, por teto atingido")
                .tag("scope", scope)
                .register(registry);
    }

    private long secondsUntilMidnight(ZonedDateTime now) {
        ZonedDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay(zone);
        return Math.max(1, Duration.between(now, midnight).toSeconds());
    }
}
