package br.com.fiap.techchallenge.usermanagement.adapter.out.security;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.fiap.techchallenge.usermanagement.application.port.out.PasswordHasher;

@Component
class BCryptPasswordHasher implements PasswordHasher {

	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

	@Override
	public String hash(String rawPassword) {
		return encoder.encode(rawPassword);
	}

	@Override
	public boolean matches(String rawPassword, String passwordHash) {
		if (rawPassword == null || rawPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
			return false;
		}
		return encoder.matches(rawPassword, passwordHash);
	}
}
