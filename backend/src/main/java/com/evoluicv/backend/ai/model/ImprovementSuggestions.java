package com.evoluicv.backend.ai.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ImprovementSuggestions(
        @JsonPropertyDescription("Lista de sugestões pontuais de melhoria, priorizadas por impacto")
        List<ImprovementSuggestion> items
) {
}
