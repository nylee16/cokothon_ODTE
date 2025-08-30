package com.odte.topicurator.llm.config;

import com.odte.topicurator.llm.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "llm.provider", havingValue = "openai")
public class OpenAiLlmClient implements LlmClient {

    private final LlmProperties props;

    private WebClient client() {
        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String chat(String model, List<ChatMessage> messages, Double temperature, Integer maxTokens) {
        Map<String,Object> body = new HashMap<>();
        body.put("model", model);
        body.put("temperature", temperature != null ? temperature : 0.2);
        if (maxTokens != null) body.put("max_tokens", maxTokens);
        body.put("messages", messages.stream().map(m -> Map.of(
                "role", m.role(),
                "content", m.content()
        )).toList());

        var resp = client().post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(props.getTimeoutMs()))
                .block();

        var choices = (List<Map<String,Object>>) resp.get("choices");
        var msg = (Map<String,Object>) choices.get(0).get("message");
        return String.valueOf(msg.get("content"));
    }
}
