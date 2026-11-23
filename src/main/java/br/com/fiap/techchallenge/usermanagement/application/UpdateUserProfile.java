package br.com.fiap.techchallenge.usermanagement.application;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

import br.com.fiap.techchallenge.usermanagement.application.exception.UserNotFoundException;
import br.com.fiap.techchallenge.usermanagement.application.model.UpdateUserProfileCommand;
import br.com.fiap.techchallenge.usermanagement.application.model.UserActor;
import br.com.fiap.techchallenge.usermanagement.application.model.UserProfile;
import br.com.fiap.techchallenge.usermanagement.application.port.out.TransactionBoundary;
import br.com.fiap.techchallenge.usermanagement.application.port.out.UserStore;
import br.com.fiap.techchallenge.usermanagement.domain.User;

public final class UpdateUserProfile {

	private final UserStore users;
	private final TransactionBoundary transactions;
	private final Clock clock;

	public UpdateUserProfile(UserStore users, TransactionBoundary transactions, Clock clock) {
		this.users = Objects.requireNonNull(users);
		this.transactions = Objects.requireNonNull(transactions);
		this.clock = Objects.requireNonNull(clock);
	}

	public UserProfile execute(UserActor actor, UpdateUserProfileCommand command) {
		return transactions.required(() -> update(actor, command));
	}

	private UserProfile update(UserActor actor, UpdateUserProfileCommand command) {
		UserAccess.requireOwnerOrSelf(actor, command.userId());
		User current = users.findById(command.userId()).orElseThrow(UserNotFoundException::new);
		User updated = current.withProfile(
				command.name(),
				command.email(),
				command.login(),
				LocalDateTime.now(clock),
				command.address());
		CreateUser.ensureIdentifiersAvailable(users, updated, command.userId());
		return UserProfile.from(users.save(updated));
	}
}
