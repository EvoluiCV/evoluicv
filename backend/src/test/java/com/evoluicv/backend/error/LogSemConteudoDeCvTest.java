package com.evoluicv.backend.error;

import com.anthropic.errors.AnthropicException;
import com.evoluicv.backend.ai.AiResponseException;
import com.evoluicv.backend.ai.CvAnalysisPort;
import com.evoluicv.backend.config.ClockConfig;
import com.evoluicv.backend.cv.CvAnalysisController;
import com.evoluicv.backend.cv.CvAnalysisService;
import com.evoluicv.backend.cv.CvTextExtractor;
import com.evoluicv.backend.quota.DailyQuota;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * LGPD: nenhum trecho do CV chega ao log, nem quando a falha carrega o conteúdo na mensagem
 * (parser de JSON, provedor de IA, erro inesperado). A marca abaixo faz o papel do CV.
 */
@WebMvcTest(controllers = CvAnalysisController.class)
@Import({CvAnalysisService.class, GlobalExceptionHandler.class, DailyQuota.class, ClockConfig.class, SimpleMeterRegistry.class})
@ExtendWith(OutputCaptureExtension.class)
class LogSemConteudoDeCvTest {

    private static final String MARCA = "Zuleika3141Pereira";

    private static final String CV = ("Zuleika3141Pereira, analista de dados com 5 anos de experiência em SQL, "
            + "Python e Power BI, automatizou relatórios e reduziu o fechamento mensal de 5 para 2 dias. ").repeat(2);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CvAnalysisPort cvAnalysisPort;

    @MockitoBean
    private CvTextExtractor cvTextExtractor;

    private ResultActions enviar(String body) throws Exception {
        return mockMvc.perform(post("/api/cv/analyze")
                .header("X-Real-IP", "203.0.113.10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions analisar() throws Exception {
        return enviar("""
                { "cvText": "%s", "professionalGoal": "Cientista de Dados" }
                """.formatted(CV));
    }

    @Test
    void jsonMalformadoVira400SemCitarOCorpoNoLog(CapturedOutput log) throws Exception {
        enviar("{ \"cvText\": " + MARCA + " }")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"));

        assertThat(log.getAll()).doesNotContain(MARCA);
    }

    @Test
    void falhaDoProvedorComTrechoDoCvNaMensagemNaoVaiParaOLog(CapturedOutput log) throws Exception {
        given(cvAnalysisPort.analyze(any(), any(), any()))
                .willThrow(new AiResponseException(AiResponseException.Reason.UPSTREAM, "falha",
                        new AnthropicException("400: input rejected near '" + MARCA + "'")));

        analisar().andExpect(status().isBadGateway());

        assertThat(log.getAll()).contains("AnthropicException").doesNotContain(MARCA);
    }

    @Test
    void erroInesperadoComTrechoDoCvNaMensagemNaoVaiParaOLog(CapturedOutput log) throws Exception {
        given(cvAnalysisPort.analyze(any(), any(), any()))
                .willThrow(new IllegalStateException("estado inválido ao processar " + MARCA));

        analisar().andExpect(status().isInternalServerError());

        assertThat(log.getAll()).contains("IllegalStateException").doesNotContain(MARCA);
    }
}
