package com.evoluicv.backend.ai.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record CvIssue(
        @JsonPropertyDescription("Seção do CV onde o problema ocorre (ex.: 'Resumo', 'Experiência — Empresa X', 'Ausente')")
        String section,

        @JsonPropertyDescription("Descrição concreta do problema encontrado")
        String problem,

        @JsonPropertyDescription("Por que isso é um problema específico para atingir o objetivo profissional informado")
        String why,

        @JsonPropertyDescription("Sugestão prática e acionável, com exemplo curto quando aplicável")
        String suggestion,

        @JsonPropertyDescription("Impacto do problema na competitividade para o objetivo: LOW, MEDIUM ou HIGH")
        Severity severity
) {
}
