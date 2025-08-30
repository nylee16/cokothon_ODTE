package com.odte.topicurator.llm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ExtractRequest(
        @NotBlank @Size(max=800)
        @Pattern(regexp="^(https?://).+$", message="URL은 http(s):// 로 시작해야 합니다.")
        String url
) {}
