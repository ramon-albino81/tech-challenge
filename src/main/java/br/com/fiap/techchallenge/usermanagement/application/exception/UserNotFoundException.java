package br.com.fiap.techchallenge.usermanagement.application.exception;

public final class UserNotFoundException extends RuntimeException {

	public UserNotFoundException() {
		super("user was not found");
	}
}
