package br.com.fiap.techchallenge.usermanagement.application;

import java.util.Objects;

import br.com.fiap.techchallenge.usermanagement.application.exception.UserNotFoundException;
import br.com.fiap.techchallenge.usermanagement.application.model.UserActor;
import br.com.fiap.techchallenge.usermanagement.application.model.UserProfile;
import br.com.fiap.techchallenge.usermanagement.application.port.out.TransactionBoundary;
import br.com.fiap.techchallenge.usermanagement.application.port.out.UserStore;

public final class GetUser {

	private final UserStore users;
	private final TransactionBoundary transactions;

	public GetUser(UserStore users, TransactionBoundary transactions) {
		this.users = Objects.requireNonNull(users);
		this.transactions = Objects.requireNonNull(transactions);
	}

	public UserProfile execute(UserActor actor, long targetUserId) {
		return transactions.readOnly(() -> {
			UserAccess.requireOwnerOrSelf(actor, targetUserId);
			return users.findById(targetUserId)
					.map(UserProfile::from)
					.orElseThrow(UserNotFoundException::new);
		});
	}
}
