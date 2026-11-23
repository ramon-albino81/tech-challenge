package br.com.fiap.techchallenge.usermanagement.application.model;

import br.com.fiap.techchallenge.usermanagement.domain.Address;
import br.com.fiap.techchallenge.usermanagement.domain.UserType;

public record CreateUserCommand(
		String name,
		String email,
		String login,
		String password,
		UserType type,
		Address address
) {
}
