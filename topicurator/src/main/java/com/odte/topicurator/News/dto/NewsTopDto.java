package com.odte.topicurator.News.dto;

import java.time.LocalDateTime;

public record NewsTopDto(
        Long id,
        String title,
        String description,
        String teaserText,
        String username,
        Long views,
        LocalDateTime createdAt,
        String imageUrl,
        String category
) {}