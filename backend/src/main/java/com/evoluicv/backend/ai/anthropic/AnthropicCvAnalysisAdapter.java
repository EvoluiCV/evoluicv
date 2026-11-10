package com.evoluicv.backend.ai.anthropic;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredOutputConfig;
import com.anthropic.models.messages.TextBlockParam;
import com.evoluicv.backend.ai.AiResponseException;
import com.evoluicv.backend.ai.AiResponseException.Reason;
import com.evoluicv.backend.ai.CvAnalysisPort;
import com.evoluicv.backend.ai.model.CvAnalysis;
import com.evoluicv.backend.ai.model.ImprovementSuggestions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Os dois agentes (recrutador e revisor) sobre a Messages API: system prompt com cache,
 * saída estruturada pelo schema do record e effort/modelo por agente. Sem parâmetros de
 * amostragem: o Haiku 5.5 rejeita temperature/top_p/top_k.
 */
public class AnthropicCvAnalysisAdapter implements CvAnalysisPort {

    private static final Logger log = LoggerFactory.getLogger(AnthropicCvAnalysisAdapter.class);

    private final AnthropicClient client;
    private final AiProperties props;
    private final String recruiterPrompt;
    private final String suggestionsPrompt;

    public AnthropicCvAnalysisAdapter(AnthropicClient client, AiProperties props) {
        this.client = client;
        this.props = props;
        this.recruiterPrompt = resource("prompts/recruiter-persona.txt");
        this.suggestionsPrompt = resource("prompts/improvement-suggestions-persona.txt");
    }

    @Override
    public CvAnalysis analyze(String professionalGoal, String targetRole, String cvText) {
        String user = """
                Objetivo profissional do candidato: %s
                Cargo alvo (opcional): %s

                Detecte o idioma do CV abaixo e responda TODA a análise nesse mesmo idioma,
                preenchendo o campo `language` com o código ISO 639-1 correspondente.

                --- CV ---
                %s
                --- FIM DO CV ---

                %s
                """.formatted(professionalGoal, targetRole, cvText, CvLanguage.directive(CvLanguage.detect(cvText), cvText));
        return call("analyst", props.analyst(), recruiterPrompt, user, CvAnalysis.class);
    }

    @Override
    public ImprovementSuggestions suggest(String language, String professionalGoal, String targetRole,
                                          String analysisJson, String cvText) {
        String user = """
                Idioma alvo (mesmo do CV original): %s
                Objetivo profissional do candidato: %s
                Cargo alvo (opcional): %s

                Parecer do recrutador (use como base para derivar edições concretas):
                %s

                --- CV ORIGINAL ---
                %s
                --- FIM DO CV ORIGINAL ---

                Gere entre 5 e 12 sugestões pontuais de melhoria, priorizadas por impacto, no idioma informado. Cada sugestão deve ser cirúrgica e acionável, sem inventar informações.

                %s
                """.formatted(language, professionalGoal, targetRole, analysisJson, cvText, CvLanguage.directive(language, cvText));
        return call("suggestions", props.suggestions(), suggestionsPrompt, user, ImprovementSuggestions.class);
    }

    private <T> T call(String agent, AiProperties.Agent cfg, String system, String user, Class<T> type) {
        var params = MessageCreateParams.builder()
                .model(cfg.model())
                .maxTokens(cfg.maxTokens())
                .systemOfTextBlockParams(List.of(TextBlockParam.builder()
                        .text(system)
                        .cacheControl(CacheControlEphemeral.builder().build())
                        .build()))
                .outputConfig(StructuredOutputConfig.<T>builder()
                        .effort(OutputConfig.Effort.of(cfg.effort()))
                        .format(type)
                        .build())
                .addUserMessage(user)
                .build();

        StructuredMessage<T> response = client.messages().create(params);
        var usage = response.rawMessage().usage();
        // só contagens: nada do conteúdo vai para o log (LGPD)
        log.info("ia agent={} model={} stop={} input={} output={} cache_read={} cache_write={}",
                agent, cfg.model(), response.rawMessage().stopReason().map(Object::toString).orElse("-"),
                usage.inputTokens(), usage.outputTokens(),
                usage.cacheReadInputTokens().orElse(0L), usage.cacheCreationInputTokens().orElse(0L));

        StopReason stop = response.rawMessage().stopReason().orElse(null);
        if (StopReason.REFUSAL.equals(stop)) {
            throw new AiResponseException(Reason.REFUSED, "O provedor recusou o conteúdo (agent=" + agent + ").");
        }
        if (StopReason.MAX_TOKENS.equals(stop)) {
            throw new AiResponseException(Reason.TRUNCATED,
                    "Resposta cortada em max_tokens=" + cfg.maxTokens() + " (agent=" + agent + ").");
        }

        try {
            return response.content().stream()
                    .flatMap(block -> block.text().stream())
                    .map(text -> text.text())
                    .findFirst()
                    .orElseThrow(() -> new AiResponseException(Reason.INVALID,
                            "Resposta sem bloco de texto (agent=" + agent + ")."));
        } catch (AiResponseException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new AiResponseException(Reason.INVALID,
                    "JSON da resposta não casa com " + type.getSimpleName() + " (agent=" + agent + ").", e);
        }
    }

    private static String resource(String path) {
        try {
            return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Prompt ausente: " + path, e);
        }
    }
}
