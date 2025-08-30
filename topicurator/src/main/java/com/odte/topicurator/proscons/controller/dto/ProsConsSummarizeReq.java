package com.odte.topicurator.proscons.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProsConsSummarizeReq(
        @NotBlank(message = "URL은 필수입니다.")
        @Size(max = 800)
        @Pattern(regexp = "^(https?://).+$", message = "URL은 http(s):// 로 시작해야 합니다.")
        String url,

        // 저장 모드일 때만 필요
        Long newsId,

        // 기본 미저장(프리뷰)
        Boolean save
) {
    public boolean saveOrDefault() { return Boolean.TRUE.equals(save); }
}
