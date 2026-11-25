package br.com.fiap.techchallenge.usermanagement.application;

import br.com.fiap.techchallenge.usermanagement.application.exception.UserForbiddenException;
import br.com.fiap.techchallenge.usermanagement.application.model.UserActor;

final class UserAccess {

	private UserAccess() {
	}

	static void requireOwner(UserActor actor) {
		if (!actor.isOwner()) {
			throw new UserForbiddenException();
		}
	}

	static void requireOwnerOrSelf(UserActor actor, long targetUserId) {
		if (!actor.isOwner() && actor.userId() != targetUserId) {
			throw new UserForbiddenException();
		}
	}
}
