package com.evoluicv.backend.cv;

import com.evoluicv.backend.ai.agent.ImprovementSuggestionsAgent;
import com.evoluicv.backend.ai.agent.RecruiterAnalystAgent;
import com.evoluicv.backend.ai.model.CvAnalysis;
import com.evoluicv.backend.ai.model.ImprovementSuggestions;
import com.evoluicv.backend.cv.dto.AnalysisResponse;
import com.evoluicv.backend.error.EmptyCvTextException;
import com.evoluicv.backend.quota.DailyQuota;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CvAnalysisService {

    private static final int MIN_TEXT_LENGTH = 200;

    private final RecruiterAnalystAgent recruiterAnalystAgent;
    private final ImprovementSuggestionsAgent improvementSuggestionsAgent;
    private final CvTextExtractor cvTextExtractor;
    private final ObjectMapper objectMapper;
    private final DailyQuota dailyQuota;

    public CvAnalysisService(
            RecruiterAnalystAgent recruiterAnalystAgent,
            ImprovementSuggestionsAgent improvementSuggestionsAgent,
            CvTextExtractor cvTextExtractor,
            ObjectMapper objectMapper,
            DailyQuota dailyQuota
    ) {
        this.recruiterAnalystAgent = recruiterAnalystAgent;
        this.improvementSuggestionsAgent = improvementSuggestionsAgent;
        this.cvTextExtractor = cvTextExtractor;
        this.objectMapper = objectMapper;
        this.dailyQuota = dailyQuota;
    }

    public AnalysisResponse analyzeFromText(String cvText, String professionalGoal, String targetRole, String clientKey) {
        return analyze(cvText, professionalGoal, targetRole, clientKey);
    }

    public AnalysisResponse analyzeFromFile(MultipartFile file, String professionalGoal, String targetRole, String clientKey) {
        String cvText = cvTextExtractor.extract(file);
        return analyze(cvText, professionalGoal, targetRole, clientKey);
    }

    private AnalysisResponse analyze(String cvText, String professionalGoal, String targetRole, String clientKey) {
        String normalized = cvText == null ? "" : cvText.strip();
        if (normalized.length() < MIN_TEXT_LENGTH) {
            throw new EmptyCvTextException(
                    "cvText precisa conter o currículo completo (mínimo " + MIN_TEXT_LENGTH + " caracteres).");
        }

        // Só análises válidas gastam cota: pedidos que falham na validação (ex.: smoke 422/400) não chamam a IA.
        dailyQuota.acquire(clientKey);

        String role = targetRole == null ? "" : targetRole;

        CvAnalysis analysis = recruiterAnalystAgent.analyze(professionalGoal, role, normalized);
        ImprovementSuggestions improvements = improvementSuggestionsAgent.suggest(
                analysis.language(),
                professionalGoal,
                role,
                serialize(analysis),
                normalized
        );

        return new AnalysisResponse(analysis, improvements);
    }

    private String serialize(CvAnalysis analysis) {
        try {
            return objectMapper.writeValueAsString(analysis);
        } catch (JsonProcessingException e) {
            // fallback: toString básico — não deve acontecer em prática para um record simples
            return analysis.toString();
        }
    }
}
