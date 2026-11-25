package br.com.fiap.techchallenge.usermanagement.application.exception;

public final class UserConflictException extends RuntimeException {

	private final DuplicateUserAttribute attribute;

	public UserConflictException(DuplicateUserAttribute attribute) {
		super("user " + attribute.name().toLowerCase() + " is already in use");
		this.attribute = attribute;
	}

	public DuplicateUserAttribute attribute() {
		return attribute;
	}
}
