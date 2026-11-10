package com.evoluicv.backend.ai.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record CvAnalysis(
        @JsonPropertyDescription("Código ISO 639-1 do idioma detectado no CV (ex.: 'pt', 'en', 'es'). A análise inteira deve estar nesse idioma.")
        String language,

        @JsonPropertyDescription("Nota geral de 0 a 100 refletindo competitividade real do CV para o objetivo profissional informado")
        int overallScore,

        @JsonPropertyDescription("Parecer executivo em 2 a 3 frases, direto e sem clichês")
        String summary,

        @JsonPropertyDescription("Pontos fortes reais do CV relevantes para o objetivo profissional informado")
        List<String> strengths,

        @JsonPropertyDescription("Problemas encontrados no CV, cada um com seção, descrição, justificativa crítica e sugestão acionável")
        List<CvIssue> issues,

        @JsonPropertyDescription("Ações recomendadas priorizadas da mais impactante para a menos impactante")
        List<String> recommendedActions
) {
}
