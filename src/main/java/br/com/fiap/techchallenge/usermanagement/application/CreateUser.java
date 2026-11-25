package br.com.fiap.techchallenge.usermanagement.application;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

import br.com.fiap.techchallenge.usermanagement.application.exception.DuplicateUserAttribute;
import br.com.fiap.techchallenge.usermanagement.application.exception.UserConflictException;
import br.com.fiap.techchallenge.usermanagement.application.model.CreateUserCommand;
import br.com.fiap.techchallenge.usermanagement.application.model.UserActor;
import br.com.fiap.techchallenge.usermanagement.application.model.UserProfile;
import br.com.fiap.techchallenge.usermanagement.application.port.out.PasswordHasher;
import br.com.fiap.techchallenge.usermanagement.application.port.out.TransactionBoundary;
import br.com.fiap.techchallenge.usermanagement.application.port.out.UserStore;
import br.com.fiap.techchallenge.usermanagement.domain.DomainValidationException;
import br.com.fiap.techchallenge.usermanagement.domain.User;

public final class CreateUser {

	private final UserStore users;
	private final PasswordHasher passwordHasher;
	private final TransactionBoundary transactions;
	private final Clock clock;

	public CreateUser(UserStore users, PasswordHasher passwordHasher, TransactionBoundary transactions, Clock clock) {
		this.users = Objects.requireNonNull(users);
		this.passwordHasher = Objects.requireNonNull(passwordHasher);
		this.transactions = Objects.requireNonNull(transactions);
		this.clock = Objects.requireNonNull(clock);
	}

	public UserProfile execute(UserActor actor, CreateUserCommand command) {
		return transactions.required(() -> create(actor, command));
	}

	private UserProfile create(UserActor actor, CreateUserCommand command) {
		UserAccess.requireOwner(actor);
		validatePassword(command.password());
		User user = new User(
				null,
				command.name(),
				command.email(),
				command.login(),
				passwordHasher.hash(command.password()),
				command.type(),
				LocalDateTime.now(clock),
				command.address());
		ensureIdentifiersAvailable(users, user, null);
		return UserProfile.from(users.save(user));
	}

	static void validatePassword(String password) {
		if (password == null || password.isBlank()
				|| password.codePointCount(0, password.length()) < 8) {
			throw new DomainValidationException("password must contain at least 8 characters");
		}
	}

	static void ensureIdentifiersAvailable(UserStore users, User user, Long currentUserId) {
		users.findByEmail(user.email()).ifPresent(existing -> {
			if (currentUserId == null || existing.id().orElseThrow() != currentUserId) {
				throw new UserConflictException(DuplicateUserAttribute.EMAIL);
			}
		});
		users.findByLogin(user.login()).ifPresent(existing -> {
			if (currentUserId == null || existing.id().orElseThrow() != currentUserId) {
				throw new UserConflictException(DuplicateUserAttribute.LOGIN);
			}
		});
	}
}
