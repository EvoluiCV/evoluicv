package com.evoluicv.backend;

import com.evoluicv.backend.ai.CvAnalysisPort;
import com.evoluicv.backend.ai.anthropic.AiProperties;
import com.evoluicv.backend.ai.anthropic.AnthropicCvAnalysisAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sobe o contexto inteiro com a configuração real: pega erro de wiring e de binding das propriedades de IA. */
@SpringBootTest(properties = "evoluicv.ai.api-key=test-key")
@AutoConfigureMockMvc
@AutoConfigureObservability(tracing = false)
class EvoluiCvApplicationTest {

    @Autowired
    private CvAnalysisPort port;

    @Autowired
    private AiProperties props;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextoSobeComOAdapterDaAnthropicEOsPadroesDosAgentes() {
        assertThat(port).isInstanceOf(AnthropicCvAnalysisAdapter.class);
        assertThat(props.analyst().model()).isEqualTo("claude-haiku-5-5");
        assertThat(props.analyst().effort()).isEqualTo("medium");
        assertThat(props.suggestions().maxTokens()).isEqualTo(16000);
    }

    @Test
    void tabelaDePrecoDoModeloPadraoEstaConfigurada() {
        assertThat(props.pricing()).containsEntry("claude-haiku-5-5", new AiProperties.Price(0.10, 0.50, 0.01, 0.125));
    }

    @Test
    void prometheusExpoeAsSeriesDeCotaQueOPainelConsulta() throws Exception {
        String body = mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).contains("evoluicv_quota_rejected_total{application=\"evoluicv-backend\",scope=\"global\"}");
    }
}
