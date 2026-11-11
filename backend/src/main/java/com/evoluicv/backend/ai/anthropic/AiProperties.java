package com.evoluicv.backend.ai.anthropic;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/** Modelo, effort e teto de tokens de cada agente, e a tabela de preço por modelo (ver application.yml). */
@ConfigurationProperties(prefix = "evoluicv.ai")
public record AiProperties(Agent analyst, Agent suggestions, Map<String, Price> pricing) {

    public AiProperties {
        pricing = pricing == null ? Map.of() : Map.copyOf(pricing);
    }

    /**
     * @param effort    low | medium | high | xhigh | max (Haiku 5.5: padrão medium)
     * @param maxTokens inclui o raciocínio adaptativo, então precisa de folga além do JSON
     */
    public record Agent(String model, String effort, long maxTokens) {
    }

    /** US$ por milhão de tokens, para o custo estimado nas métricas. Não é cobrança: confira na fatura. */
    public record Price(double input, double output, double cacheRead, double cacheWrite) {
    }
}
