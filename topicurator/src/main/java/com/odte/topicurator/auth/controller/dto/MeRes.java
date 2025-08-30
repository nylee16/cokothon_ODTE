package com.odte.topicurator.auth.controller.dto;

import java.time.LocalDateTime;

public record MeRes(
        Long id,
        String email,
        String username,
        String sex,
        Short birthYear,
        String job,
        LocalDateTime createdAt
) {}
