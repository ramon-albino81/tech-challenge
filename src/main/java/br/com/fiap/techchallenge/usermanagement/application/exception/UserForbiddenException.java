package br.com.fiap.techchallenge.usermanagement.application.exception;

public final class UserForbiddenException extends RuntimeException {

	public UserForbiddenException() {
		super("operation is not allowed");
	}
}
