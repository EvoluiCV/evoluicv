package com.evoluicv.backend.quota;

import com.evoluicv.backend.ai.AiResponseException;
import com.evoluicv.backend.ai.CvAnalysisPort;
import com.evoluicv.backend.ai.model.CvAnalysis;
import com.evoluicv.backend.ai.model.ImprovementSuggestions;
import com.evoluicv.backend.cv.CvAnalysisController;
import com.evoluicv.backend.cv.CvAnalysisService;
import com.evoluicv.backend.cv.CvTextExtractor;
import com.evoluicv.backend.error.GlobalExceptionHandler;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cotas diárias vistas de fora: requisições HTTP reais passando pelo filtro de rate limit,
 * agentes de IA mockados e relógio controlado pelo teste.
 */
@WebMvcTest(controllers = CvAnalysisController.class)
@Import({CvAnalysisService.class, GlobalExceptionHandler.class, DailyQuota.class, DailyQuotaTest.ClockConfig.class})
@TestPropertySource(properties = {
        "evoluicv.quota.per-ip-daily=3",
        "evoluicv.quota.global-daily=5",
        "evoluicv.quota.zone=America/Sao_Paulo"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DailyQuotaTest {

    /** 2026-11-10 23:00 em São Paulo (UTC-3): faltam 3600 s para a meia-noite local. */
    private static final Instant ONZE_DA_NOITE_SP = Instant.parse("2026-11-11T02:00:00Z");

    private static final String CV_VALIDO = ("Maria Souza — Analista de Dados. Experiência: 5 anos em "
            + "SQL, Python e Power BI na Empresa Z, automatizou relatórios e reduziu o fechamento "
            + "mensal de 5 para 2 dias. Formação: Estatística, UNICAMP. Inglês avançado. ").repeat(2);

    @TestConfiguration
    static class ClockConfig {
        @Bean
        MutableClock clock() {
            return new MutableClock(ONZE_DA_NOITE_SP);
        }

        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MutableClock clock;

    @Autowired
    private MeterRegistry metrics;

    @MockitoBean
    private CvAnalysisPort cvAnalysisPort;

    @MockitoBean
    private CvTextExtractor cvTextExtractor;

    @BeforeEach
    void agentesRespondem() {
        given(cvAnalysisPort.analyze(any(), any(), any())).willReturn(
                new CvAnalysis("pt", 70, "ok", List.of(), List.of(), List.of()));
        given(cvAnalysisPort.suggest(any(), any(), any(), any())).willReturn(
                new ImprovementSuggestions(List.of()));
    }

    private ResultActions analisar(String ip) throws Exception {
        return analisar(ip, CV_VALIDO);
    }

    private ResultActions analisar(String ip, String cvText) throws Exception {
        String body = """
                { "cvText": "%s", "professionalGoal": "Cientista de Dados" }
                """.formatted(cvText);
        return mockMvc.perform(post("/api/cv/analyze")
                .header("X-Real-IP", ip)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Test
    void quartaAnaliseDoMesmoIpNoDiaRecebe429ComRetryAfterAteAMeiaNoite() throws Exception {
        for (int i = 0; i < 3; i++) {
            analisar("203.0.113.10").andExpect(status().isOk());
        }

        analisar("203.0.113.10")
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "3600"))
                .andExpect(jsonPath("$.error").value("daily_quota_exceeded"));
        assertThat(rejeitadas("ip")).isEqualTo(1);
    }

    @Test
    void ipDiferenteTemCotaPropria() throws Exception {
        for (int i = 0; i < 3; i++) {
            analisar("203.0.113.10").andExpect(status().isOk());
        }

        analisar("198.51.100.20").andExpect(status().isOk());
    }

    @Test
    void tetoGlobalBarraAteIpNovoQuandoEsgota() throws Exception {
        analisar("203.0.113.1").andExpect(status().isOk());
        analisar("203.0.113.1").andExpect(status().isOk());
        analisar("203.0.113.2").andExpect(status().isOk());
        analisar("203.0.113.2").andExpect(status().isOk());
        analisar("203.0.113.3").andExpect(status().isOk());

        analisar("203.0.113.4")
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "3600"))
                .andExpect(jsonPath("$.error").value("daily_quota_exceeded"));
        assertThat(rejeitadas("global")).isEqualTo(1);
        assertThat(rejeitadas("ip")).isZero();
    }

    private double rejeitadas(String scope) {
        return metrics.get("evoluicv.quota.rejected").tag("scope", scope).counter().count();
    }

    @Test
    void pedidoQueFalhaNaValidacaoNaoGastaCota() throws Exception {
        // 2 inválidas + 3 válidas = 5, dentro do limite de rajada do filtro (5/min por IP).
        for (int i = 0; i < 2; i++) {
            analisar("203.0.113.10", "curto demais").andExpect(status().isUnprocessableEntity());
        }

        for (int i = 0; i < 3; i++) {
            analisar("203.0.113.10").andExpect(status().isOk());
        }
    }

    @Test
    void falhaDoProvedorDevolveACota() throws Exception {
        // 2 falhas + 3 sucessos = 5, dentro da rajada; sem devolução, a terceira válida já seria barrada.
        given(cvAnalysisPort.analyze(any(), any(), any()))
                .willThrow(new AiResponseException(AiResponseException.Reason.UPSTREAM, "529"))
                .willThrow(new AiResponseException(AiResponseException.Reason.INVALID, "schema"))
                .willReturn(new CvAnalysis("pt", 70, "ok", List.of(), List.of(), List.of()));

        analisar("203.0.113.10").andExpect(status().isBadGateway());
        analisar("203.0.113.10").andExpect(status().isBadGateway());
        for (int i = 0; i < 3; i++) {
            analisar("203.0.113.10").andExpect(status().isOk());
        }
    }

    @Test
    void recusaDoProvedorGastaACota() throws Exception {
        // recusa é sobre o conteúdo enviado: devolver a cota deixaria repetir sem limite
        given(cvAnalysisPort.analyze(any(), any(), any()))
                .willThrow(new AiResponseException(AiResponseException.Reason.REFUSED, "refusal"));

        for (int i = 0; i < 3; i++) {
            analisar("203.0.113.10").andExpect(status().isUnprocessableEntity());
        }
        analisar("203.0.113.10").andExpect(status().isTooManyRequests());
    }

    @Test
    void cotaZeraAMeiaNoiteDeSaoPaulo() throws Exception {
        for (int i = 0; i < 3; i++) {
            analisar("203.0.113.10").andExpect(status().isOk());
        }
        analisar("203.0.113.10").andExpect(status().isTooManyRequests());

        clock.set(Instant.parse("2026-11-11T03:00:00Z")); // 00:00 de 11/11 em São Paulo

        analisar("203.0.113.10").andExpect(status().isOk());
    }

    /** Relógio que o teste avança; o fuso vem da configuração da cota, não do relógio. */
    static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void set(Instant instant) {
            this.now = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
