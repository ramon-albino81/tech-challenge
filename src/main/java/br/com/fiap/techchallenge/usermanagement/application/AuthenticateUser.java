package br.com.fiap.techchallenge.usermanagement.application;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

import br.com.fiap.techchallenge.usermanagement.application.exception.InvalidCredentialsException;
import br.com.fiap.techchallenge.usermanagement.application.model.AccessToken;
import br.com.fiap.techchallenge.usermanagement.application.port.out.AccessTokenIssuer;
import br.com.fiap.techchallenge.usermanagement.application.port.out.PasswordHasher;
import br.com.fiap.techchallenge.usermanagement.application.port.out.TransactionBoundary;
import br.com.fiap.techchallenge.usermanagement.application.port.out.UserStore;
import br.com.fiap.techchallenge.usermanagement.domain.User;

public final class AuthenticateUser {

	private final UserStore users;
	private final PasswordHasher passwordHasher;
	private final AccessTokenIssuer tokens;
	private final TransactionBoundary transactions;

	public AuthenticateUser(
			UserStore users,
			PasswordHasher passwordHasher,
			AccessTokenIssuer tokens,
			TransactionBoundary transactions
	) {
		this.users = Objects.requireNonNull(users);
		this.passwordHasher = Objects.requireNonNull(passwordHasher);
		this.tokens = Objects.requireNonNull(tokens);
		this.transactions = Objects.requireNonNull(transactions);
	}

	public AccessToken execute(String login, String password) {
		return transactions.readOnly(() -> authenticate(login, password));
	}

	private AccessToken authenticate(String login, String password) {
		if (login == null || login.isBlank() || password == null || password.isEmpty()
				|| password.getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new InvalidCredentialsException();
		}
		User user = users.findByLogin(login.strip().toLowerCase(Locale.ROOT))
				.filter(candidate -> passwordHasher.matches(password, candidate.passwordHash()))
				.orElseThrow(InvalidCredentialsException::new);
		AccessTokenIssuer.IssuedToken issued = tokens.issue(user);
		return new AccessToken(issued.value(), issued.expiresAt());
	}
}
