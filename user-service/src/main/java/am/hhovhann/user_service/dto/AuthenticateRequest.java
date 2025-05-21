package am.hhovhann.user_service.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthenticateRequest (@NotBlank String username, @NotBlank String password, @NotBlank String email) {}
