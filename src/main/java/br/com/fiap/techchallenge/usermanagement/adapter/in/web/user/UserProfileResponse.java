package br.com.fiap.techchallenge.usermanagement.adapter.in.web.user;

import java.time.LocalDateTime;

import br.com.fiap.techchallenge.usermanagement.application.model.UserProfile;
import br.com.fiap.techchallenge.usermanagement.domain.UserType;

public record UserProfileResponse(
		long id,
		String name,
		String email,
		String login,
		UserType type,
		LocalDateTime lastModifiedAt,
		AddressResponse address
) {

	static UserProfileResponse from(UserProfile profile) {
		return new UserProfileResponse(
				profile.id(),
				profile.name(),
				profile.email(),
				profile.login(),
				profile.type(),
				profile.lastModifiedAt(),
				AddressResponse.from(profile.address()));
	}
}
