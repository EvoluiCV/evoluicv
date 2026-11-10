package com.evoluicv.backend.ai.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record ImprovementSuggestion(
        @JsonPropertyDescription("Tipo de ação: ADD (incluir algo novo), REMOVE (remover), REWRITE (reescrever/ajustar um trecho existente), IMPROVE (refinar mantendo a ideia)")
        ImprovementAction action,

        @JsonPropertyDescription("Seção do CV afetada (ex.: 'Resumo', 'Experiência — Empresa X', 'Habilidades', 'Formação')")
        String section,

        @JsonPropertyDescription("Trecho atual do CV que será alterado ou removido. Deve ser null/vazio quando action=ADD.")
        String current,

        @JsonPropertyDescription("Texto concreto sugerido (o que deve ficar no lugar, ou o que deve ser adicionado). Deve ser null/vazio quando action=REMOVE.")
        String proposed,

        @JsonPropertyDescription("Motivo objetivo — por que essa mudança melhora o CV para o objetivo profissional do candidato")
        String rationale,

        @JsonPropertyDescription("Impacto esperado da mudança: HIGH, MEDIUM ou LOW")
        Severity impact
) {
}
