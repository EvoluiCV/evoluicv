package com.evoluicv.backend.ai.anthropic;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Modelo, effort e teto de tokens de cada agente, vindos de env (ver application.yml). */
@ConfigurationProperties(prefix = "evoluicv.ai")
public record AiProperties(Agent analyst, Agent suggestions) {

    /**
     * @param effort    low | medium | high | xhigh | max (Haiku 5.5: padrão medium)
     * @param maxTokens inclui o raciocínio adaptativo, então precisa de folga além do JSON
     */
    public record Agent(String model, String effort, long maxTokens) {
    }
}
