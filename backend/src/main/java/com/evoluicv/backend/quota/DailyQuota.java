package com.evoluicv.backend.quota;

import org.springframework.beans.factory.annotation.Value;
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
public class DailyQuota {

    private final Clock clock;
    private final ZoneId zone;
    private final int perIpDaily;
    private final int globalDaily;

    private LocalDate day;
    private int usedGlobal;
    private final Map<String, Integer> usedByIp = new HashMap<>();

    public DailyQuota(
            Clock clock,
            @Value("${evoluicv.quota.zone:America/Sao_Paulo}") String zone,
            @Value("${evoluicv.quota.per-ip-daily:3}") int perIpDaily,
            @Value("${evoluicv.quota.global-daily:200}") int globalDaily
    ) {
        this.clock = clock;
        this.zone = ZoneId.of(zone);
        this.perIpDaily = perIpDaily;
        this.globalDaily = globalDaily;
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
            throw new DailyQuotaExceededException(secondsUntilMidnight(now));
        }
        usedByIp.put(clientKey, used + 1);
        usedGlobal++;
    }

    private long secondsUntilMidnight(ZonedDateTime now) {
        ZonedDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay(zone);
        return Math.max(1, Duration.between(now, midnight).toSeconds());
    }
}
