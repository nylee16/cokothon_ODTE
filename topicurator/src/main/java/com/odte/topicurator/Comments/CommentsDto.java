package com.odte.topicurator.Comments;

import java.time.LocalDateTime;

public record CommentsDto (
        Long id,
        Long prosconsId,
        Long userId,
        String content,
        String username,
        String choice,
        Long likeCount,
        Long hateCount,
        LocalDateTime createdAt
) {}
