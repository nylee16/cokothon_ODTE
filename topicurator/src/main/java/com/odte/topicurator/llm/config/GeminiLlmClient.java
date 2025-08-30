package com.odte.topicurator.llm.config;

import com.odte.topicurator.llm.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "llm.provider", havingValue = "gemini")
public class GeminiLlmClient implements LlmClient {

    private final LlmProperties props;

    private WebClient client() {
        return WebClient.builder()
                .baseUrl(props.getBaseUrl()) // https://generativelanguage.googleapis.com
                .build();
    }

    @Override
    public String chat(String model, List<ChatMessage> messages, Double temperature, Integer maxTokens) {
        // 간단히 user 메시지들만 합쳐서 하나의 text로 보냄
        StringBuilder sb = new StringBuilder();
        for (var m : messages) {
            if (!"assistant".equals(m.role())) sb.append(m.content()).append("\n");
        }
        Map<String,Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", sb.toString()))))
        );

        var resp = client().post()
                .uri(uri -> uri.path("/v1beta/models/"+model+":generateContent")
                        .queryParam("key", props.getApiKey()).build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(props.getTimeoutMs()))
                .block();

        var candidates = (List<Map<String,Object>>) resp.get("candidates");
        var content = (Map<String,Object>) candidates.get(0).get("content");
        var parts = (List<Map<String,Object>>) content.get("parts");
        return String.valueOf(parts.get(0).get("text"));
    }
}
