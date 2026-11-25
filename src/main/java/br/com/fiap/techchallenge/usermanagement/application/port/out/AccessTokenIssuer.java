package br.com.fiap.techchallenge.usermanagement.application.port.out;

import java.time.Instant;

import br.com.fiap.techchallenge.usermanagement.domain.User;

public interface AccessTokenIssuer {

	IssuedToken issue(User user);

	record IssuedToken(String value, Instant expiresAt) {
	}
}
