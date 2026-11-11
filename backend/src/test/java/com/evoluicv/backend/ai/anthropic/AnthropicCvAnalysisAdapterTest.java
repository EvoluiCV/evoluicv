package com.evoluicv.backend.ai.anthropic;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.evoluicv.backend.ai.AiResponseException;
import com.evoluicv.backend.ai.model.CvAnalysis;
import com.evoluicv.backend.ai.model.ImprovementSuggestions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * O adapter visto pela fronteira HTTP da Anthropic: uma API falsa local grava o corpo de cada
 * requisição e devolve uma resposta pronta. Nenhuma chamada sai da máquina.
 */
class AnthropicCvAnalysisAdapterTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    private static final String ANALISE_JSON = """
            {"language":"pt","overallScore":72,"summary":"Bom CV.","strengths":["Java"],\
            "issues":[{"section":"Resumo","problem":"p","why":"w","suggestion":"s","severity":"HIGH"}],\
            "recommendedActions":["Adicionar links"]}""";

    private static final String SUGESTOES_JSON = """
            {"items":[{"action":"ADD","section":"Contato","current":"","proposed":"github.com/[usuario]",\
            "rationale":"r","impact":"HIGH"}]}""";

    private HttpServer server;
    private final List<JsonNode> requests = new ArrayList<>();
    private volatile String nextResponse;
    private volatile int nextStatus = 200;
    private AnthropicCvAnalysisAdapter adapter;
    private final SimpleMeterRegistry metrics = new SimpleMeterRegistry();

    @BeforeEach
    void startFakeApi() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/messages", exchange -> {
            requests.add(JSON.readTree(exchange.getRequestBody()));
            byte[] body = nextResponse.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(nextStatus, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        AnthropicClient client = AnthropicOkHttpClient.builder()
                .apiKey("test-key")
                .baseUrl("http://127.0.0.1:" + server.getAddress().getPort())
                .maxRetries(0)
                .build();
        AiProperties props = new AiProperties(
                new AiProperties.Agent("claude-haiku-5-5", "low", 8000),
                new AiProperties.Agent("claude-haiku-5-5", "medium", 12000),
                // preços de teste (US$ por milhão de tokens), escolhidos para dar contas redondas
                Map.of("claude-haiku-5-5", new AiProperties.Price(1.0, 5.0, 0.1, 1.25))
        );
        adapter = new AnthropicCvAnalysisAdapter(client, props, metrics);
    }

    @AfterEach
    void stopFakeApi() {
        server.stop(0);
    }

    private static String message(String text, String stopReason) throws IOException {
        return JSON.writeValueAsString(JSON.readTree("""
                {"id":"msg_test","type":"message","role":"assistant","model":"claude-haiku-5-5",
                 "content":[{"type":"text","text":%s}],
                 "stop_reason":"%s","stop_sequence":null,
                 "usage":{"input_tokens":1200,"output_tokens":300,
                          "cache_read_input_tokens":2000,"cache_creation_input_tokens":0}}
                """.formatted(JSON.writeValueAsString(text), stopReason)));
    }

    @Test
    void analiseEnviaModeloEffortCacheESchemaSemTemperatureEDevolveORecord() throws IOException {
        nextResponse = message(ANALISE_JSON, "end_turn");

        CvAnalysis analise = adapter.analyze("Tech Lead", "Tech Lead Backend", "CV de teste");

        assertThat(analise.language()).isEqualTo("pt");
        assertThat(analise.overallScore()).isEqualTo(72);
        assertThat(analise.issues()).hasSize(1);

        JsonNode req = requests.getFirst();
        assertThat(req.path("model").asText()).isEqualTo("claude-haiku-5-5");
        assertThat(req.path("max_tokens").asLong()).isEqualTo(8000);
        assertThat(req.path("output_config").path("effort").asText()).isEqualTo("low");
        assertThat(req.path("output_config").path("format").path("type").asText()).isEqualTo("json_schema");
        assertThat(req.path("output_config").path("format").path("schema").path("properties").has("overallScore")).isTrue();
        assertThat(req.has("temperature")).isFalse();
        assertThat(req.path("system").get(0).path("cache_control").path("type").asText()).isEqualTo("ephemeral");
        assertThat(req.path("system").get(0).path("text").asText()).contains("recrutador sênior");
        assertThat(req.path("messages").get(0).toString()).contains("CV de teste").contains("Tech Lead Backend");
    }

    @Test
    void cvEmInglesRecebeOrdemDeIdiomaEmInglesNaMensagem() throws IOException {
        nextResponse = message(ANALISE_JSON, "end_turn");

        adapter.analyze("Staff Engineer", "", """
                Senior engineer with 10 years building backend systems. I led the migration of the
                checkout service and mentored the team. Experience at a streaming company where I
                designed the event platform that is used by all of the product teams.""");

        assertThat(requests.getFirst().path("messages").get(0).toString())
                .contains("The CV is written in English")
                .doesNotContain("O CV está em português");
    }

    @Test
    void cvEmPortuguesRecebeOrdemDeIdiomaEmPortugues() throws IOException {
        nextResponse = message(ANALISE_JSON, "end_turn");

        adapter.analyze("Tech Lead", "", "Engenheira de software com 9 anos de experiência em backend Java e que lidera um time de pagamentos.");

        assertThat(requests.getFirst().path("messages").get(0).toString())
                .contains("O CV está em português")
                .doesNotContain("The CV is written in English");
    }

    @Test
    void sugestoesUsamModeloEEffortDoProprioAgente() throws IOException {
        nextResponse = message(SUGESTOES_JSON, "end_turn");

        ImprovementSuggestions sugestoes = adapter.suggest("pt", "Tech Lead", "", "{}", "CV de teste");

        assertThat(sugestoes.items()).hasSize(1);
        JsonNode req = requests.getFirst();
        assertThat(req.path("output_config").path("effort").asText()).isEqualTo("medium");
        assertThat(req.path("max_tokens").asLong()).isEqualTo(12000);
        assertThat(req.path("system").get(0).path("text").asText()).contains("revisor sênior");
    }

    @Test
    void recusaDoProvedorViraErroDeRecusa() throws IOException {
        nextResponse = message("", "refusal");

        assertThatThrownBy(() -> adapter.analyze("Tech Lead", "", "CV de teste"))
                .isInstanceOfSatisfying(AiResponseException.class,
                        e -> assertThat(e.reason()).isEqualTo(AiResponseException.Reason.REFUSED));
    }

    @Test
    void respostaCortadaEmMaxTokensViraErroDeTruncamento() throws IOException {
        nextResponse = message("{\"language\":\"pt\",\"overallSc", "max_tokens");

        assertThatThrownBy(() -> adapter.analyze("Tech Lead", "", "CV de teste"))
                .isInstanceOfSatisfying(AiResponseException.class,
                        e -> assertThat(e.reason()).isEqualTo(AiResponseException.Reason.TRUNCATED));
    }

    @Test
    void cadaChamadaContaTokensPorTipoECustoEstimadoPorAgente() throws IOException {
        nextResponse = message(ANALISE_JSON, "end_turn");

        adapter.analyze("Tech Lead", "", "CV de teste");

        assertThat(tokens("analyst", "input")).isEqualTo(1200);
        assertThat(tokens("analyst", "output")).isEqualTo(300);
        assertThat(tokens("analyst", "cache_read")).isEqualTo(2000);
        assertThat(tokens("analyst", "cache_write")).isEqualTo(0);
        // 1200 x 1,00 + 300 x 5,00 + 2000 x 0,10 = 2900 por milhão
        assertThat(metrics.get("evoluicv.ai.cost").tag("agent", "analyst").tag("model", "claude-haiku-5-5")
                .counter().count()).isCloseTo(0.0029, org.assertj.core.data.Offset.offset(1e-9));
    }

    private double tokens(String agent, String type) {
        return metrics.get("evoluicv.ai.tokens").tag("agent", agent).tag("type", type).counter().count();
    }

    @Test
    void latenciaPorAgenteSeparaChamadaOkDeRecusada() throws IOException {
        nextResponse = message(SUGESTOES_JSON, "end_turn");
        adapter.suggest("pt", "Tech Lead", "", "{}", "CV de teste");
        nextResponse = message("", "refusal");
        assertThatThrownBy(() -> adapter.analyze("Tech Lead", "", "CV de teste"));

        assertThat(calls("suggestions", "ok")).isEqualTo(1);
        assertThat(calls("analyst", "refused")).isEqualTo(1);
        assertThat(metrics.find("evoluicv.ai.calls").tag("agent", "analyst").tag("outcome", "ok").timer()).isNull();
    }

    private long calls(String agent, String outcome) {
        return metrics.get("evoluicv.ai.calls").tag("agent", agent).tag("outcome", outcome).timer().count();
    }

    @Test
    void falhaHttpDoProvedorViraErroUpstreamSemExporOSdkEContaNaMetrica() {
        nextStatus = 529;
        nextResponse = """
                {"type":"error","error":{"type":"overloaded_error","message":"Overloaded"}}""";

        assertThatThrownBy(() -> adapter.analyze("Tech Lead", "", "CV de teste"))
                .isInstanceOfSatisfying(AiResponseException.class,
                        e -> assertThat(e.reason()).isEqualTo(AiResponseException.Reason.UPSTREAM));
        assertThat(calls("analyst", "upstream")).isEqualTo(1);
    }
}
