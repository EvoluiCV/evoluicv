package com.evoluicv.backend.ai.anthropic;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.evoluicv.backend.ai.CvAnalysisPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AnthropicConfig {

    @Bean(destroyMethod = "close")
    public AnthropicClient anthropicClient(@Value("${evoluicv.ai.api-key}") String apiKey) {
        return AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                // cada agente leva ~10-30 s; acima de 2 min algo travou e é melhor devolver 502
                .timeout(Duration.ofMinutes(2))
                .maxRetries(2)
                .build();
    }

    @Bean
    public CvAnalysisPort cvAnalysisPort(AnthropicClient client, AiProperties props) {
        return new AnthropicCvAnalysisAdapter(client, props);
    }
}
