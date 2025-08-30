package com.odte.topicurator.Comments;

import jakarta.validation.constraints.NotBlank;

public record CommentsCreateRequest(
        @NotBlank
        String content,
        String choice
) {}
