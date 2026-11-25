package br.com.fiap.techchallenge.usermanagement.application.model;

import br.com.fiap.techchallenge.usermanagement.domain.DomainValidationException;
import br.com.fiap.techchallenge.usermanagement.domain.UserType;

public record UserActor(long userId, UserType type) {

	public UserActor {
		if (userId <= 0) {
			throw new DomainValidationException("userId must be positive");
		}
		if (type == null) {
			throw new DomainValidationException("type is required");
		}
	}

	public boolean isOwner() {
		return type == UserType.DONO_RESTAURANTE;
	}
}
