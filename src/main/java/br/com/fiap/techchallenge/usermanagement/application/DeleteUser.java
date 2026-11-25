package br.com.fiap.techchallenge.usermanagement.application;

import java.util.Objects;

import br.com.fiap.techchallenge.usermanagement.application.exception.UserNotFoundException;
import br.com.fiap.techchallenge.usermanagement.application.model.UserActor;
import br.com.fiap.techchallenge.usermanagement.application.port.out.TransactionBoundary;
import br.com.fiap.techchallenge.usermanagement.application.port.out.UserStore;

public final class DeleteUser {

	private final UserStore users;
	private final TransactionBoundary transactions;

	public DeleteUser(UserStore users, TransactionBoundary transactions) {
		this.users = Objects.requireNonNull(users);
		this.transactions = Objects.requireNonNull(transactions);
	}

	public void execute(UserActor actor, long targetUserId) {
		transactions.required(() -> {
			UserAccess.requireOwnerOrSelf(actor, targetUserId);
			if (users.findById(targetUserId).isEmpty()) {
				throw new UserNotFoundException();
			}
			users.deleteById(targetUserId);
			return null;
		});
	}
}
