package br.com.fiap.techchallenge.usermanagement.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

class UserTest {

	private static final Address ADDRESS = new Address("Rua A", "12", "Cidade", "01000-000");
	private static final LocalDateTime LAST_MODIFIED_AT = LocalDateTime.parse("2026-11-19T12:00:00");

	@Test
	void createsUserWithNormalizedEmailAndLogin() {
		User user = createUser(null, " Maria Silva ", " MARIA@EXAMPLE.COM ", " Maria.Login ");

		assertFalse(user.id().isPresent());
		assertEquals("Maria Silva", user.name());
		assertEquals("maria@example.com", user.email());
		assertEquals("maria.login", user.login());
		assertEquals(UserType.CLIENTE, user.type());
		assertEquals(LAST_MODIFIED_AT, user.lastModifiedAt());
		assertEquals(ADDRESS, user.address());
	}

	@Test
	void assignsPersistentIdWithoutMutatingOriginalUser() {
		User user = createUser(null, "Maria Silva", "maria@example.com", "maria");
		User persistedUser = user.withId(10);

		assertTrue(user.id().isEmpty());
		assertEquals(10, persistedUser.id().orElseThrow());
	}

	@Test
	void rejectsAnInvalidPersistentId() {
		User user = createUser(null, "Maria Silva", "maria@example.com", "maria");

		assertThrows(DomainValidationException.class, () -> user.withId(0));
		assertThrows(DomainValidationException.class, () -> user.withId(-1));
	}

	@ParameterizedTest
	@MethodSource("missingRequiredFields")
	void rejectsMissingRequiredFields(String name, String email, String login, String passwordHash,
			UserType type, LocalDateTime lastModifiedAt, Address address) {
		assertThrows(DomainValidationException.class,
				() -> new User(null, name, email, login, passwordHash, type, lastModifiedAt, address));
	}

	private static Stream<Arguments> missingRequiredFields() {
		return Stream.of(
				Arguments.of(null, "maria@example.com", "maria", "$2a$hash", UserType.CLIENTE, LAST_MODIFIED_AT, ADDRESS),
				Arguments.of("Maria", null, "maria", "$2a$hash", UserType.CLIENTE, LAST_MODIFIED_AT, ADDRESS),
				Arguments.of("Maria", "maria@example.com", " ", "$2a$hash", UserType.CLIENTE, LAST_MODIFIED_AT, ADDRESS),
				Arguments.of("Maria", "maria@example.com", "maria", null, UserType.CLIENTE, LAST_MODIFIED_AT, ADDRESS),
				Arguments.of("Maria", "maria@example.com", "maria", "$2a$hash", null, LAST_MODIFIED_AT, ADDRESS),
				Arguments.of("Maria", "maria@example.com", "maria", "$2a$hash", UserType.CLIENTE, null, ADDRESS),
				Arguments.of("Maria", "maria@example.com", "maria", "$2a$hash", UserType.CLIENTE, LAST_MODIFIED_AT, null));
	}

	@ParameterizedTest
	@MethodSource("invalidEmails")
	void rejectsInvalidEmailFormat(String email) {
		assertThrows(DomainValidationException.class,
				() -> createUser(null, "Maria", email, "maria"));
	}

	private static Stream<String> invalidEmails() {
		return Stream.of("maria", "maria@", "@example.com", "maria@example", "maria example@example.com");
	}

	@Test
	void rejectsFieldsLongerThanThePersistenceLimits() {
		assertThrows(DomainValidationException.class,
				() -> createUser(null, "n".repeat(151), "maria@example.com", "maria"));
		assertThrows(DomainValidationException.class,
				() -> createUser(null, "Maria", "a".repeat(243) + "@example.com", "maria"));
		assertThrows(DomainValidationException.class,
				() -> createUser(null, "Maria", "maria@example.com", "l".repeat(101)));
	}

	@Test
	void rejectsUnsupportedUserTypes() {
		assertThrows(DomainValidationException.class, () -> UserType.fromValue("ADMIN"));
		assertThrows(DomainValidationException.class, () -> UserType.fromValue(null));
	}

	@Test
	void acceptsBothSupportedUserTypes() {
		assertEquals(UserType.DONO_RESTAURANTE, UserType.fromValue("dono_restaurante"));
		assertEquals(UserType.CLIENTE, UserType.fromValue("cliente"));
	}

	private static User createUser(Long id, String name, String email, String login) {
		return new User(id, name, email, login, "$2a$hash", UserType.CLIENTE, LAST_MODIFIED_AT, ADDRESS);
	}
}
