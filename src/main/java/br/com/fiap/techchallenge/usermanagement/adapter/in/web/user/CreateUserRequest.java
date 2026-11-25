package br.com.fiap.techchallenge.usermanagement.adapter.in.web.user;

import br.com.fiap.techchallenge.usermanagement.application.model.CreateUserCommand;
import br.com.fiap.techchallenge.usermanagement.domain.UserType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
		@NotBlank @Size(max = 150) String name,
		@NotBlank @Email @Size(max = 254) String email,
		@NotBlank @Size(min = 8, max = 72) String password,
		@NotBlank @Size(max = 100) String login,
		@NotNull UserType type,
		@NotNull @Valid AddressRequest address
) {

	CreateUserCommand toCommand() {
		return new CreateUserCommand(name, email, login, password, type, address.toDomain());
	}
}
