package br.com.fiap.techchallenge.usermanagement.domain;

import java.util.Locale;

public enum UserType {
	DONO_RESTAURANTE,
	CLIENTE;

	public static UserType fromValue(String value) {
		if (value == null) {
			throw new DomainValidationException("type is required");
		}
		try {
			return valueOf(value.strip().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			throw new DomainValidationException("type is not supported");
		}
	}
}
