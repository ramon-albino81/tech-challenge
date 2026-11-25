package br.com.fiap.techchallenge.usermanagement.application;

import java.util.List;
import java.util.Objects;

import br.com.fiap.techchallenge.usermanagement.application.model.UserActor;
import br.com.fiap.techchallenge.usermanagement.application.model.UserProfile;
import br.com.fiap.techchallenge.usermanagement.application.model.UserProfilePage;
import br.com.fiap.techchallenge.usermanagement.application.port.out.TransactionBoundary;
import br.com.fiap.techchallenge.usermanagement.application.port.out.UserStore;
import br.com.fiap.techchallenge.usermanagement.domain.DomainValidationException;

public final class SearchUsers {

	public static final int DEFAULT_PAGE = 0;
	public static final int DEFAULT_SIZE = 20;
	public static final int MAX_SIZE = 100;

	private final UserStore users;
	private final TransactionBoundary transactions;

	public SearchUsers(UserStore users, TransactionBoundary transactions) {
		this.users = Objects.requireNonNull(users);
		this.transactions = Objects.requireNonNull(transactions);
	}

	public UserProfilePage execute(UserActor actor, String name, int page, int size) {
		return transactions.readOnly(() -> {
			UserAccess.requireOwner(actor);
			if (name == null || name.isBlank()) {
				throw new DomainValidationException("name is required");
			}
			if (page < 0 || size < 1 || size > MAX_SIZE) {
				throw new DomainValidationException("page or size is outside the supported range");
			}
			var result = users.searchByName(name.strip(), page, size);
			List<UserProfile> profiles = result.users().stream().map(UserProfile::from).toList();
			return new UserProfilePage(profiles, result.page(), result.size(), result.totalElements());
		});
	}

	public UserProfilePage execute(UserActor actor, String name, Integer page, Integer size) {
		return execute(
				actor,
				name,
				page == null ? DEFAULT_PAGE : page,
				size == null ? DEFAULT_SIZE : size);
	}
}
