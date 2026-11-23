package br.com.fiap.techchallenge.usermanagement.application;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

import br.com.fiap.techchallenge.usermanagement.application.exception.InvalidCurrentPasswordException;
import br.com.fiap.techchallenge.usermanagement.application.exception.UserNotFoundException;
import br.com.fiap.techchallenge.usermanagement.application.model.UserActor;
import br.com.fiap.techchallenge.usermanagement.application.port.out.PasswordHasher;
import br.com.fiap.techchallenge.usermanagement.application.port.out.TransactionBoundary;
import br.com.fiap.techchallenge.usermanagement.application.port.out.UserStore;
import br.com.fiap.techchallenge.usermanagement.domain.User;

public final class ChangeOwnPassword {

	private final UserStore users;
	private final PasswordHasher passwordHasher;
	private final TransactionBoundary transactions;
	private final Clock clock;

	public ChangeOwnPassword(
			UserStore users,
			PasswordHasher passwordHasher,
			TransactionBoundary transactions,
			Clock clock
	) {
		this.users = Objects.requireNonNull(users);
		this.passwordHasher = Objects.requireNonNull(passwordHasher);
		this.transactions = Objects.requireNonNull(transactions);
		this.clock = Objects.requireNonNull(clock);
	}

	public void execute(UserActor actor, String currentPassword, String newPassword) {
		transactions.required(() -> {
			change(actor, currentPassword, newPassword);
			return null;
		});
	}

	private void change(UserActor actor, String currentPassword, String newPassword) {
		CreateUser.validatePassword(newPassword);
		User current = users.findById(actor.userId()).orElseThrow(UserNotFoundException::new);
		if (currentPassword == null || !passwordHasher.matches(currentPassword, current.passwordHash())) {
			throw new InvalidCurrentPasswordException();
		}
		users.save(current.withPasswordHash(passwordHasher.hash(newPassword), LocalDateTime.now(clock)));
	}
}
