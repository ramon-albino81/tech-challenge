package br.com.fiap.techchallenge.usermanagement.adapter.in.web.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank String login,
		@NotBlank String password
) {
}
