package com.odte.topicurator.llm.config;

import com.odte.topicurator.llm.dto.ChatMessage;
import java.util.List;

public interface LlmClient {
    String chat(String model, List<ChatMessage> messages, Double temperature, Integer maxTokens);
}
