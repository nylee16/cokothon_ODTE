package com.odte.topicurator.llm.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ChatRequest(
        @NotEmpty List<ChatMessage> messages,
        Double temperature,
        Integer maxTokens
) {}
