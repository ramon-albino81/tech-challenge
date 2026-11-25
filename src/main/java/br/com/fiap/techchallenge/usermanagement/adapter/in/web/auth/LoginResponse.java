package br.com.fiap.techchallenge.usermanagement.adapter.in.web.auth;

import java.time.Instant;

public record LoginResponse(String accessToken, String tokenType, Instant expiresAt) {
}
