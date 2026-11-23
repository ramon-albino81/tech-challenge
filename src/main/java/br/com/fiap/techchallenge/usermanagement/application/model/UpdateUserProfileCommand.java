package br.com.fiap.techchallenge.usermanagement.application.model;

import br.com.fiap.techchallenge.usermanagement.domain.Address;

public record UpdateUserProfileCommand(
		long userId,
		String name,
		String email,
		String login,
		Address address
) {
}
