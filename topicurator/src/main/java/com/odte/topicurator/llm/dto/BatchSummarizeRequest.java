package com.odte.topicurator.llm.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BatchSummarizeRequest(
        @NotEmpty List<String> urls,
        String language
) {
    public String languageOrDefault(){ return (language==null||language.isBlank()) ? "ko" : language; }
}
