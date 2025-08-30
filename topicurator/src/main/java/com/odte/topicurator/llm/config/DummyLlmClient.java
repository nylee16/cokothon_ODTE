package com.odte.topicurator.llm.config;

import com.odte.topicurator.llm.dto.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ConditionalOnMissingBean(LlmClient.class) // 다른 클라이언트 없으면 기본 사용
public class DummyLlmClient implements LlmClient {
    @Override
    public String chat(String model, List<ChatMessage> messages, Double temperature, Integer maxTokens) {
        log.info("[DUMMY-LLM] model={}, messages={}", model, messages.size());
        // JSON 스키마 흉내(서버에서 파싱 가능)
        return """
               {"summary":"더미 요약","pros":"찬성 포인트 더미","neutral":"중립 더미","cons":"반대 포인트 더미",
                "bias":0,"biasReason":"-","confidence":60}
               """;
    }
}
