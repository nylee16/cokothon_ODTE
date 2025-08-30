package com.odte.topicurator.proscons.controller.dto;

public record ProsConsRes(
        Long id,
        Long newsId,
        Long authorId,
        String authorUsername,
        String summary,
        String link,
        String pros,
        String neutral,
        String cons,
        int bias
) {}
