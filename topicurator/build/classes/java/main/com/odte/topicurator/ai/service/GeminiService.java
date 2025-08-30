package com.odte.topicurator.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odte.topicurator.ai.dto.NewsResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    public GeminiService(GeminiConfig geminiConfig) {
        this.geminiConfig = geminiConfig;
    }

    public NewsResponse analyzeNews(String link) throws Exception {
        String prompt = String.format(
                "다음 뉴스 링크의 내용을 바탕으로 JSON만 출력하세요:\n" +
                        "{\n" +
                        "  \"link\": \"%s\",\n" +
                        "  \"summary\": \"200자 요약\",\n" +
                        "  \"pros\": \"200자 찬성 의견\",\n" +
                        "  \"cons\": \"200자 반대 의견\",\n" +
                        "  \"neutral_bias\": \"중립적 비율(%%)\",\n" +
                        "  \"unneutral_bias\": \"편향적 비율(%%)\"\n" +
                        "}\n", link
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                GEMINI_URL + "?key=" + geminiConfig.getApiKey(),
                HttpMethod.POST,
                entity,
                String.class
        );

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode contentNode = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");

        JsonNode resultJson = objectMapper.readTree(contentNode.asText());

        return new NewsResponse(
                resultJson.path("link").asText(),
                resultJson.path("summary").asText(),
                resultJson.path("pros").asText(),
                resultJson.path("cons").asText(),
                resultJson.path("neutral_bias").asText(),
                resultJson.path("unneutral_bias").asText()
        );
    }
}
