package br.com.fiap.techchallenge.usermanagement.application.exception;

public final class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("login or password is invalid");
	}
}
