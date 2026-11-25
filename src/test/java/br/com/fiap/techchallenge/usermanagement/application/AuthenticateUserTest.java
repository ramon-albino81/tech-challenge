package br.com.fiap.techchallenge.usermanagement.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.techchallenge.usermanagement.application.exception.InvalidCredentialsException;
import br.com.fiap.techchallenge.usermanagement.application.port.out.AccessTokenIssuer;
import br.com.fiap.techchallenge.usermanagement.application.port.out.PasswordHasher;
import br.com.fiap.techchallenge.usermanagement.application.port.out.TransactionBoundary;
import br.com.fiap.techchallenge.usermanagement.application.port.out.UserStore;
import br.com.fiap.techchallenge.usermanagement.domain.Address;
import br.com.fiap.techchallenge.usermanagement.domain.User;
import br.com.fiap.techchallenge.usermanagement.domain.UserType;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserTest {

	@Mock
	private UserStore users;

	@Mock
	private PasswordHasher passwordHasher;

	@Mock
	private AccessTokenIssuer tokens;

	private final TransactionBoundary transactions = new TransactionBoundary() {
		@Override
		public <T> T required(Supplier<T> operation) {
			return operation.get();
		}

		@Override
		public <T> T readOnly(Supplier<T> operation) {
			return operation.get();
		}
	};

	@Test
	void authenticatesNormalizedLoginAndReturnsIssuedToken() {
		User user = user();
		Instant expiresAt = Instant.parse("2026-11-24T21:40:00Z");
		when(users.findByLogin("avaliador")).thenReturn(Optional.of(user));
		when(passwordHasher.matches("senha-segura-123", user.passwordHash())).thenReturn(true);
		when(tokens.issue(user)).thenReturn(new AccessTokenIssuer.IssuedToken("signed-token", expiresAt));

		var result = new AuthenticateUser(users, passwordHasher, tokens, transactions)
				.execute("  AVALIADOR  ", "senha-segura-123");

		assertEquals("signed-token", result.value());
		assertEquals(expiresAt, result.expiresAt());
		verify(passwordHasher).matches("senha-segura-123", user.passwordHash());
	}

	@Test
	void rejectsUnknownLoginAndIncorrectPasswordWithSameException() {
		when(users.findByLogin("inexistente")).thenReturn(Optional.empty());
		assertThrows(
				InvalidCredentialsException.class,
				() -> new AuthenticateUser(users, passwordHasher, tokens, transactions)
						.execute("inexistente", "senha-incorreta"));

		User user = user();
		when(users.findByLogin("avaliador")).thenReturn(Optional.of(user));
		when(passwordHasher.matches("senha-incorreta", user.passwordHash())).thenReturn(false);
		assertThrows(
				InvalidCredentialsException.class,
				() -> new AuthenticateUser(users, passwordHasher, tokens, transactions)
						.execute("avaliador", "senha-incorreta"));
	}

	@Test
	void rejectsMissingCredentials() {
		AuthenticateUser authenticateUser = new AuthenticateUser(users, passwordHasher, tokens, transactions);

		assertThrows(InvalidCredentialsException.class, () -> authenticateUser.execute(null, "senha"));
		assertThrows(InvalidCredentialsException.class, () -> authenticateUser.execute("avaliador", ""));
		assertThrows(
				InvalidCredentialsException.class,
				() -> authenticateUser.execute("avaliador", "a".repeat(73)));
	}

	private static User user() {
		return new User(
				1L,
				"Avaliador",
				"avaliador@example.invalid",
				"avaliador",
				"$2a$12$hash",
				UserType.DONO_RESTAURANTE,
				LocalDateTime.of(2026, 11, 24, 21, 0),
				new Address("Rua", "1", "Cidade", "01000-000"));
	}
}
