package com.odte.topicurator.llm.dto;

public record SummarizeResponse(
        String title,
        String link,
        String summary,
        String pros,
        String neutral,
        String cons,
        int bias,              // -100 ~ +100
        String biasReason,     // nullable
        Integer confidence     // 0~100, nullable (LLM가 neutrality 등으로 줄 때 매핑)
) {}
