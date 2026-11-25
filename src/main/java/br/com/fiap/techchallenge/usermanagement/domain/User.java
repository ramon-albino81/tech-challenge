package br.com.fiap.techchallenge.usermanagement.domain;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.OptionalLong;
import java.util.regex.Pattern;

public final class User {

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
	private final Long id;
	private final String name;
	private final String email;
	private final String login;
	private final String passwordHash;
	private final UserType type;
	private final LocalDateTime lastModifiedAt;
	private final Address address;

	public User(
			Long id,
			String name,
			String email,
			String login,
			String passwordHash,
			UserType type,
			LocalDateTime lastModifiedAt,
			Address address
	) {
		if (id != null && id <= 0) {
			throw new DomainValidationException("id must be positive");
		}
		this.id = id;
		this.name = required("name", name, 150);
		this.email = normalizeEmail(email);
		this.login = normalizeLogin(login);
		this.passwordHash = required("passwordHash", passwordHash, 255);
		this.type = required("type", type);
		this.lastModifiedAt = required("lastModifiedAt", lastModifiedAt);
		this.address = required("address", address);
	}

	public OptionalLong id() {
		return id == null ? OptionalLong.empty() : OptionalLong.of(id);
	}

	public String name() {
		return name;
	}

	public String email() {
		return email;
	}

	public String login() {
		return login;
	}

	public String passwordHash() {
		return passwordHash;
	}

	public UserType type() {
		return type;
	}

	public LocalDateTime lastModifiedAt() {
		return lastModifiedAt;
	}

	public Address address() {
		return address;
	}

	public User withId(long persistedId) {
		if (id != null) {
			throw new DomainValidationException("id is already assigned");
		}
		return new User(persistedId, name, email, login, passwordHash, type, lastModifiedAt, address);
	}

	public User withProfile(String updatedName, String updatedEmail, String updatedLogin,
			LocalDateTime updatedAt, Address updatedAddress) {
		return new User(id, updatedName, updatedEmail, updatedLogin, passwordHash, type, updatedAt, updatedAddress);
	}

	public User withPasswordHash(String updatedPasswordHash, LocalDateTime updatedAt) {
		return new User(id, name, email, login, updatedPasswordHash, type, updatedAt, address);
	}

	private static String normalizeEmail(String value) {
		String email = required("email", value, 254).toLowerCase(Locale.ROOT);
		if (!EMAIL_PATTERN.matcher(email).matches()) {
			throw new DomainValidationException("email must be valid");
		}
		return email;
	}

	private static String normalizeLogin(String value) {
		return required("login", value, 100).toLowerCase(Locale.ROOT);
	}

	private static String required(String field, String value, int maxLength) {
		if (value == null || value.isBlank()) {
			throw new DomainValidationException(field + " is required");
		}
		String normalized = value.strip();
		if (normalized.codePointCount(0, normalized.length()) > maxLength) {
			throw new DomainValidationException(field + " exceeds maximum length");
		}
		return normalized;
	}

	private static <T> T required(String field, T value) {
		if (value == null) {
			throw new DomainValidationException(field + " is required");
		}
		return value;
	}
}
