package com.evoluicv.backend.ai.anthropic;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.evoluicv.backend.ai.AiResponseException;
import com.evoluicv.backend.ai.model.CvAnalysis;
import com.evoluicv.backend.ai.model.ImprovementSuggestions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
    private AnthropicCvAnalysisAdapter adapter;

    @BeforeEach
    void startFakeApi() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/messages", exchange -> {
            requests.add(JSON.readTree(exchange.getRequestBody()));
            byte[] body = nextResponse.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
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
                new AiProperties.Agent("claude-haiku-5-5", "medium", 12000)
        );
        adapter = new AnthropicCvAnalysisAdapter(client, props);
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
                 "usage":{"input_tokens":1200,"output_tokens":300}}
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
}
