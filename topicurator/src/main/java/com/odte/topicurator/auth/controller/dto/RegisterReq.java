package com.odte.topicurator.auth.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterReq(
        @NotBlank @Email String email,
        @NotBlank String username,
        @NotBlank @Size(min=8, max=50) String password,
        String sex,
        Short birthYear,
        String job
) {}
