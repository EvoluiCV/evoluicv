package com.evoluicv.backend;

import com.evoluicv.backend.ai.CvAnalysisPort;
import com.evoluicv.backend.ai.anthropic.AiProperties;
import com.evoluicv.backend.ai.anthropic.AnthropicCvAnalysisAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/** Sobe o contexto inteiro com a configuração real: pega erro de wiring e de binding das propriedades de IA. */
@SpringBootTest(properties = "evoluicv.ai.api-key=test-key")
class EvoluiCvApplicationTest {

    @Autowired
    private CvAnalysisPort port;

    @Autowired
    private AiProperties props;

    @Test
    void contextoSobeComOAdapterDaAnthropicEOsPadroesDosAgentes() {
        assertThat(port).isInstanceOf(AnthropicCvAnalysisAdapter.class);
        assertThat(props.analyst().model()).isEqualTo("claude-haiku-5-5");
        assertThat(props.analyst().effort()).isEqualTo("medium");
        assertThat(props.suggestions().maxTokens()).isEqualTo(16000);
    }
}
