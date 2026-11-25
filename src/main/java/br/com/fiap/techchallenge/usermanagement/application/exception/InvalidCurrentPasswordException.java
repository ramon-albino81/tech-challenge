package br.com.fiap.techchallenge.usermanagement.application.exception;

public final class InvalidCurrentPasswordException extends RuntimeException {

	public InvalidCurrentPasswordException() {
		super("current password is invalid");
	}
}
