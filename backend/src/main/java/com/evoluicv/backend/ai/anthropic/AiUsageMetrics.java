package com.evoluicv.backend.ai.anthropic;

import com.anthropic.models.messages.Usage;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.TimeUnit;

/**
 * Tokens por tipo e custo estimado por agente, no Micrometer (exposto em /actuator/prometheus).
 * Modelo sem preço configurado conta tokens e custo zero.
 */
final class AiUsageMetrics {

    private static final double PER_MILLION = 1_000_000.0;

    private final MeterRegistry registry;
    private final AiProperties props;

    AiUsageMetrics(MeterRegistry registry, AiProperties props) {
        this.registry = registry;
        this.props = props;
    }

    void record(String agent, String model, Usage usage) {
        long input = usage.inputTokens();
        long output = usage.outputTokens();
        long cacheRead = usage.cacheReadInputTokens().orElse(0L);
        long cacheWrite = usage.cacheCreationInputTokens().orElse(0L);

        tokens(agent, model, "input", input);
        tokens(agent, model, "output", output);
        tokens(agent, model, "cache_read", cacheRead);
        tokens(agent, model, "cache_write", cacheWrite);

        AiProperties.Price price = props.pricing().get(model);
        double cost = price == null ? 0 : (input * price.input() + output * price.output()
                + cacheRead * price.cacheRead() + cacheWrite * price.cacheWrite()) / PER_MILLION;
        Counter.builder("evoluicv.ai.cost")
                .description("Custo estimado das chamadas à IA, pela tabela evoluicv.ai.pricing")
                .baseUnit("usd")
                .tag("agent", agent).tag("model", model)
                .register(registry)
                .increment(cost);
    }

    /** @param outcome ok | refused | truncated | invalid | error (falha HTTP ou de rede com o provedor) */
    void call(String agent, String model, String outcome, long nanos) {
        Timer.builder("evoluicv.ai.calls")
                .description("Duração das chamadas à IA por agente e resultado")
                .tag("agent", agent).tag("model", model).tag("outcome", outcome)
                .register(registry)
                .record(nanos, TimeUnit.NANOSECONDS);
    }

    private void tokens(String agent, String model, String type, long count) {
        Counter.builder("evoluicv.ai.tokens")
                .description("Tokens cobrados pela API da IA, por tipo")
                .tag("agent", agent).tag("model", model).tag("type", type)
                .register(registry)
                .increment(count);
    }
}
