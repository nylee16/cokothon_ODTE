package com.odte.topicurator.auth.controller.dto;

public record RegisterReq(
        String email,
        String username,
        String password,
        String sex,
        Short birthYear,
        String job
) {}
