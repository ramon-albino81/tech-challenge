package br.com.fiap.techchallenge.usermanagement.application.model;

import java.time.LocalDateTime;

import br.com.fiap.techchallenge.usermanagement.domain.Address;
import br.com.fiap.techchallenge.usermanagement.domain.User;
import br.com.fiap.techchallenge.usermanagement.domain.UserType;

public record UserProfile(
		long id,
		String name,
		String email,
		String login,
		UserType type,
		LocalDateTime lastModifiedAt,
		Address address
) {

	public static UserProfile from(User user) {
		return new UserProfile(
				user.id().orElseThrow(() -> new IllegalStateException("persisted user must have an id")),
				user.name(),
				user.email(),
				user.login(),
				user.type(),
				user.lastModifiedAt(),
				user.address());
	}
}
