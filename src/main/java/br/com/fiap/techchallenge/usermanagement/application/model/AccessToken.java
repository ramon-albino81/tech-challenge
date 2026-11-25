package br.com.fiap.techchallenge.usermanagement.application.model;

import java.time.Instant;

public record AccessToken(String value, Instant expiresAt) {
}
