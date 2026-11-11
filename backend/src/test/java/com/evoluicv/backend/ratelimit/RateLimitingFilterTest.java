package com.evoluicv.backend.ratelimit;

import com.evoluicv.backend.ai.CvAnalysisPort;
import com.evoluicv.backend.config.ClockConfig;
import com.evoluicv.backend.cv.CvAnalysisController;
import com.evoluicv.backend.cv.CvAnalysisService;
import com.evoluicv.backend.cv.CvTextExtractor;
import com.evoluicv.backend.error.GlobalExceptionHandler;
import com.evoluicv.backend.quota.DailyQuota;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Limite de rajada (5/min por IP) visto de fora. Os pedidos são reprovados na validação (422),
 * então não gastam cota diária nem chamam a IA: só o filtro decide entre passar e 429.
 */
@WebMvcTest(controllers = CvAnalysisController.class)
@Import({CvAnalysisService.class, GlobalExceptionHandler.class, DailyQuota.class, ClockConfig.class, SimpleMeterRegistry.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD) // buckets ficam no bean do filtro, compartilhado pelo contexto em cache
class RateLimitingFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CvAnalysisPort cvAnalysisPort;

    @MockitoBean
    private CvTextExtractor cvTextExtractor;

    private ResultActions enviar(String ip) throws Exception {
        return mockMvc.perform(post("/api/cv/analyze")
                .header("X-Real-IP", ip)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"cvText\": \"curto demais\", \"professionalGoal\": \"Dev\" }"));
    }

    @Test
    void sextoPedidoNoMesmoMinutoRecebe429ComRetryAfter() throws Exception {
        for (int restantes = 4; restantes >= 0; restantes--) {
            enviar("203.0.113.10")
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(header().string("X-RateLimit-Remaining", String.valueOf(restantes)));
        }

        enviar("203.0.113.10")
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.error").value("rate_limit_exceeded"));
    }

    @Test
    void ipDistintoTemBucketDistinto() throws Exception {
        for (int i = 0; i < 6; i++) {
            enviar("203.0.113.10");
        }

        enviar("198.51.100.20")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(header().string("X-RateLimit-Remaining", "4"));
    }
}
