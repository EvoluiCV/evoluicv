package com.evoluicv.backend.ai;

import com.evoluicv.backend.ai.model.CvAnalysis;
import com.evoluicv.backend.ai.model.ImprovementSuggestions;

/**
 * Fronteira com o provedor de LLM. O serviço só conhece este port; o adapter da Anthropic
 * é o único que sabe de modelo, effort, cache e formato da API.
 */
public interface CvAnalysisPort {

    /** Parecer do recrutador sobre o CV, no idioma do próprio CV. */
    CvAnalysis analyze(String professionalGoal, String targetRole, String cvText);

    /** Edições concretas derivadas do parecer, no idioma informado. */
    ImprovementSuggestions suggest(
            String language,
            String professionalGoal,
            String targetRole,
            String analysisJson,
            String cvText
    );
}
