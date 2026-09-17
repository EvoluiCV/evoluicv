package com.evoluicv.backend.cv;

import com.evoluicv.backend.ai.agent.ImprovementSuggestionsAgent;
import com.evoluicv.backend.ai.agent.RecruiterAnalystAgent;
import com.evoluicv.backend.ai.model.CvAnalysis;
import com.evoluicv.backend.ai.model.CvIssue;
import com.evoluicv.backend.ai.model.ImprovementAction;
import com.evoluicv.backend.ai.model.ImprovementSuggestion;
import com.evoluicv.backend.ai.model.ImprovementSuggestions;
import com.evoluicv.backend.ai.model.Severity;
import com.evoluicv.backend.error.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CvAnalysisController.class)
// RateLimitingFilter is a @Component (Filter), so @WebMvcTest picks it up and shares
// its buckets across the whole cached test context; disable it here, it has no test of its own yet.
@AutoConfigureMockMvc(addFilters = false)
@Import({CvAnalysisService.class, GlobalExceptionHandler.class})
class CvAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecruiterAnalystAgent recruiterAnalystAgent;

    @MockitoBean
    private ImprovementSuggestionsAgent improvementSuggestionsAgent;

    @MockitoBean
    private CvTextExtractor cvTextExtractor;

    private static final String LONG_CV = """
            João Silva — Engenheiro de Software
            Experiência
            - Desenvolvedor Backend na Empresa X (2020-2024): liderou refatoração
              de microsserviços, reduzindo latência média em 35% e custos em 20%.
            - Estagiário na Empresa Y (2018-2020): automações internas em Python.
            Formação: Bacharelado em Ciência da Computação, USP (2016-2020).
            Tecnologias: Java, Spring Boot, Kafka, PostgreSQL, Docker, Kubernetes.
            Idiomas: Português nativo, Inglês avançado.
            """;

    @Test
    void analisaCvViaJsonRetornandoAnaliseEstruturada() throws Exception {
        CvAnalysis analise = new CvAnalysis(
                "pt",
                72,
                "CV competitivo para Tech Lead, mas precisa amarrar resultados ao contexto de fintech.",
                List.of("Experiência sólida com Java/Spring", "Resultados quantificados"),
                List.of(new CvIssue(
                        "Resumo",
                        "Falta posicionamento explícito como Tech Lead em fintech",
                        "Recrutadores descartam em 10s CVs sem keyword do papel alvo",
                        "Adicione headline: 'Tech Lead em Fintech com foco em plataformas de pagamento'",
                        Severity.HIGH
                )),
                List.of("Reescrever headline", "Destacar liderança técnica")
        );

        ImprovementSuggestions sugestoes = new ImprovementSuggestions(List.of(
                new ImprovementSuggestion(
                        ImprovementAction.ADD,
                        "Resumo",
                        null,
                        "Tech Lead em Fintech | Java/Spring | Plataformas de pagamento de alta escala",
                        "Adiciona keyword do papel alvo logo no topo do CV",
                        Severity.HIGH
                ),
                new ImprovementSuggestion(
                        ImprovementAction.REWRITE,
                        "Experiência — Empresa X",
                        "liderou refatoração de microsserviços",
                        "Liderou refatoração de microsserviços críticos para pagamentos, reduzindo latência em 35%",
                        "Conecta a experiência ao contexto de fintech",
                        Severity.MEDIUM
                )
        ));

        given(recruiterAnalystAgent.analyze(eq("Tech Lead fintech"), any(), any())).willReturn(analise);
        given(improvementSuggestionsAgent.suggest(any(), any(), any(), any(), any())).willReturn(sugestoes);

        String body = objectMapper.writeValueAsString(new java.util.LinkedHashMap<>() {{
            put("cvText", LONG_CV.repeat(3));
            put("professionalGoal", "Tech Lead fintech");
        }});

        mockMvc.perform(post("/api/cv/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis.language").value("pt"))
                .andExpect(jsonPath("$.analysis.overallScore").value(72))
                .andExpect(jsonPath("$.analysis.issues[0].severity").value("HIGH"))
                .andExpect(jsonPath("$.analysis.issues[0].why").exists())
                .andExpect(jsonPath("$.improvements.items[0].action").value("ADD"))
                .andExpect(jsonPath("$.improvements.items[0].proposed").exists())
                .andExpect(jsonPath("$.improvements.items[1].action").value("REWRITE"))
                .andExpect(jsonPath("$.improvements.items[1].current").exists());
    }

    @Test
    void retorna400QuandoProfessionalGoalEstaAusente() throws Exception {
        String body = """
                { "cvText": "%s" }
                """.formatted(LONG_CV.repeat(3).replace("\n", " "));

        mockMvc.perform(post("/api/cv/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_error"));
    }

    @Test
    void retorna422QuandoCvEhMuitoCurto() throws Exception {
        String body = """
                { "cvText": "curto demais", "professionalGoal": "Tech Lead" }
                """;

        mockMvc.perform(post("/api/cv/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("empty_cv"));
    }

    @Test
    void retorna400QuandoMultipartSemProfessionalGoal() throws Exception {
        mockMvc.perform(multipart("/api/cv/analyze")
                        .param("cvText", LONG_CV.repeat(3)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"));
    }

    @Test
    void retorna405QuandoMetodoNaoSuportado() throws Exception {
        mockMvc.perform(get("/api/cv/analyze"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").value("method_not_allowed"));
    }
}
