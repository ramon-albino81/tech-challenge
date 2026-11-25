package br.com.fiap.techchallenge.usermanagement.adapter.in.web.user;

import br.com.fiap.techchallenge.usermanagement.application.model.UpdateUserProfileCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
		@NotBlank @Size(max = 150) String name,
		@NotBlank @Email @Size(max = 254) String email,
		@NotBlank @Size(max = 100) String login,
		@NotNull @Valid AddressRequest address
) {

	UpdateUserProfileCommand toCommand(long userId) {
		return new UpdateUserProfileCommand(userId, name, email, login, address.toDomain());
	}
}
