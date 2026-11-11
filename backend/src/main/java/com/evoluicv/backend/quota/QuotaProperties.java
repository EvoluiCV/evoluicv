package com.evoluicv.backend.quota;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZoneId;

/**
 * Cota diária de análises (ver application.yml, que guarda os padrões).
 *
 * @param perIpDaily  análises por IP no dia; 0 desliga o limite por IP (staging)
 * @param globalDaily análises no total do dia, teto de custo; precisa ser positivo
 * @param zone        fuso em que a cota zera à meia-noite
 */
@ConfigurationProperties(prefix = "evoluicv.quota")
public record QuotaProperties(int perIpDaily, int globalDaily, ZoneId zone) {

    public QuotaProperties {
        if (perIpDaily < 0 || globalDaily <= 0 || zone == null) {
            throw new IllegalArgumentException(
                    "evoluicv.quota inválida: per-ip-daily >= 0, global-daily > 0 e zone obrigatória");
        }
    }
}
