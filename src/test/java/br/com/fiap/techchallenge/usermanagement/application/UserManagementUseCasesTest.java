package br.com.fiap.techchallenge.usermanagement.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.techchallenge.usermanagement.application.exception.DuplicateUserAttribute;
import br.com.fiap.techchallenge.usermanagement.application.exception.InvalidCurrentPasswordException;
import br.com.fiap.techchallenge.usermanagement.application.exception.UserConflictException;
import br.com.fiap.techchallenge.usermanagement.application.exception.UserForbiddenException;
import br.com.fiap.techchallenge.usermanagement.application.exception.UserNotFoundException;
import br.com.fiap.techchallenge.usermanagement.application.model.CreateUserCommand;
import br.com.fiap.techchallenge.usermanagement.application.model.UpdateUserProfileCommand;
import br.com.fiap.techchallenge.usermanagement.application.model.UserActor;
import br.com.fiap.techchallenge.usermanagement.application.model.UserProfile;
import br.com.fiap.techchallenge.usermanagement.application.model.UserProfilePage;
import br.com.fiap.techchallenge.usermanagement.application.model.UserSearchPage;
import br.com.fiap.techchallenge.usermanagement.application.port.out.PasswordHasher;
import br.com.fiap.techchallenge.usermanagement.application.port.out.TransactionBoundary;
import br.com.fiap.techchallenge.usermanagement.application.port.out.UserStore;
import br.com.fiap.techchallenge.usermanagement.domain.Address;
import br.com.fiap.techchallenge.usermanagement.domain.DomainValidationException;
import br.com.fiap.techchallenge.usermanagement.domain.User;
import br.com.fiap.techchallenge.usermanagement.domain.UserType;

@ExtendWith(MockitoExtension.class)
class UserManagementUseCasesTest {

	private static final Address ADDRESS = new Address("Rua A", "12", "Cidade", "01000-000");
	private static final UserActor OWNER = new UserActor(1, UserType.DONO_RESTAURANTE);
	private static final UserActor CLIENT = new UserActor(2, UserType.CLIENTE);
	private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 19, 15, 0);

	@Mock
	private UserStore users;

	@Mock
	private PasswordHasher passwordHasher;

	private final TransactionBoundary transactions = new TransactionBoundary() {
		@Override
		public <T> T required(Supplier<T> operation) {
			return operation.get();
		}

		@Override
		public <T> T readOnly(Supplier<T> operation) {
			return operation.get();
		}
	};

	private final Clock clock = Clock.fixed(Instant.parse("2026-11-19T15:00:00Z"), ZoneOffset.UTC);

	@Test
	void ownerCreatesUserWithNormalizedIdentifiersAndNoSecretInProfile() {
		when(passwordHasher.hash("senha-segura-123")).thenReturn("$2a$hash");
		when(users.findByEmail("maria@example.com")).thenReturn(Optional.empty());
		when(users.findByLogin("maria")).thenReturn(Optional.empty());
		when(users.save(any(User.class))).thenAnswer(invocation -> ((User) invocation.getArgument(0)).withId(10));

		UserProfile profile = new CreateUser(users, passwordHasher, transactions, clock)
				.execute(OWNER, new CreateUserCommand(
						"Maria Silva",
						" MARIA@EXAMPLE.COM ",
						" Maria ",
						"senha-segura-123",
						UserType.CLIENTE,
						ADDRESS));

		assertEquals(10, profile.id());
		assertEquals("maria@example.com", profile.email());
		assertEquals("maria", profile.login());
		assertEquals(NOW, profile.lastModifiedAt());
		assertFalse(List.of(UserProfile.class.getRecordComponents()).stream()
				.anyMatch(component -> component.getName().toLowerCase().contains("password")));
		verify(users).save(any(User.class));
	}

	@Test
	void clientCannotCreateUsers() {
		assertThrows(
				UserForbiddenException.class,
				() -> new CreateUser(users, passwordHasher, transactions, clock).execute(
						CLIENT,
						new CreateUserCommand("Maria", "maria@example.com", "maria", "senha-segura-123",
								UserType.CLIENTE, ADDRESS)));

		verify(users, never()).save(any(User.class));
		verify(passwordHasher, never()).hash("senha-segura-123");
	}

	@Test
	void createRejectsDuplicateEmailOrLogin() {
		when(passwordHasher.hash("senha-segura-123")).thenReturn("$2a$hash");
		User existing = user(11, "outra@example.com", "outra");
		when(users.findByEmail("outra@example.com")).thenReturn(Optional.of(existing));

		UserConflictException emailConflict = assertThrows(
				UserConflictException.class,
				() -> new CreateUser(users, passwordHasher, transactions, clock).execute(
						OWNER,
						new CreateUserCommand("Maria", "outra@example.com", "maria", "senha-segura-123",
								UserType.CLIENTE, ADDRESS)));
		assertEquals(DuplicateUserAttribute.EMAIL, emailConflict.attribute());

		when(users.findByEmail("maria@example.com")).thenReturn(Optional.empty());
		when(users.findByLogin("outra")).thenReturn(Optional.of(existing));
		UserConflictException loginConflict = assertThrows(
				UserConflictException.class,
				() -> new CreateUser(users, passwordHasher, transactions, clock).execute(
						OWNER,
						new CreateUserCommand("Maria", "maria@example.com", "outra", "senha-segura-123",
								UserType.CLIENTE, ADDRESS)));
		assertEquals(DuplicateUserAttribute.LOGIN, loginConflict.attribute());
	}

	@Test
	void createRejectsShortPasswordsBeforeHashing() {
		assertThrows(
				DomainValidationException.class,
				() -> new CreateUser(users, passwordHasher, transactions, clock).execute(
						OWNER,
						new CreateUserCommand("Maria", "maria@example.com", "maria", "curta",
								UserType.CLIENTE, ADDRESS)));

		verify(passwordHasher, never()).hash("curta");
	}

	@Test
	void profileUpdatePreservesPasswordAndTypeAndChangesTimestamp() {
		User existing = user(2, "maria@example.com", "maria");
		when(users.findById(2)).thenReturn(Optional.of(existing));
		when(users.findByEmail("maria@example.com")).thenReturn(Optional.of(existing));
		when(users.findByLogin("maria")).thenReturn(Optional.of(existing));
		when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		UserProfile updated = new UpdateUserProfile(users, transactions, clock).execute(
				CLIENT,
				new UpdateUserProfileCommand(2, "Maria Nova", "maria@example.com", "maria", ADDRESS));

		assertEquals("Maria Nova", updated.name());
		assertEquals(UserType.CLIENTE, updated.type());
		assertEquals(NOW, updated.lastModifiedAt());
		verify(users).save(org.mockito.ArgumentMatchers.argThat(
				user -> user.passwordHash().equals("$2a$hash") && user.type() == UserType.CLIENTE));
	}

	@Test
	void profileUpdateRejectsOtherUsersAndMissingUsers() {
		assertThrows(
				UserForbiddenException.class,
				() -> new UpdateUserProfile(users, transactions, clock).execute(
						CLIENT,
						new UpdateUserProfileCommand(3, "Maria", "maria@example.com", "maria", ADDRESS)));
		assertThrows(
				UserNotFoundException.class,
				() -> new UpdateUserProfile(users, transactions, clock).execute(
						OWNER,
						new UpdateUserProfileCommand(3, "Maria", "maria@example.com", "maria", ADDRESS)));
	}

	@Test
	void changeOwnPasswordRequiresCurrentPasswordAndUpdatesTimestamp() {
		User existing = user(2, "maria@example.com", "maria");
		when(users.findById(2)).thenReturn(Optional.of(existing));
		when(passwordHasher.matches("senha-atual", "$2a$hash")).thenReturn(true);
		when(passwordHasher.hash("senha-nova-123")).thenReturn("$2a$new-hash");

		new ChangeOwnPassword(users, passwordHasher, transactions, clock)
				.execute(CLIENT, "senha-atual", "senha-nova-123");

		verify(users).save(org.mockito.ArgumentMatchers.argThat(
				user -> user.passwordHash().equals("$2a$new-hash")
						&& user.lastModifiedAt().equals(NOW)
						&& user.type() == UserType.CLIENTE));
	}

	@Test
	void changeOwnPasswordRejectsIncorrectCurrentAndShortNewPassword() {
		User existing = user(2, "maria@example.com", "maria");
		when(users.findById(2)).thenReturn(Optional.of(existing));
		when(passwordHasher.matches("errada", "$2a$hash")).thenReturn(false);

		assertThrows(
				InvalidCurrentPasswordException.class,
				() -> new ChangeOwnPassword(users, passwordHasher, transactions, clock)
						.execute(CLIENT, "errada", "senha-nova-123"));
		assertThrows(
				DomainValidationException.class,
				() -> new ChangeOwnPassword(users, passwordHasher, transactions, clock)
						.execute(CLIENT, "senha-atual", "curta"));
		verify(users, never()).save(any(User.class));
	}

	@Test
	void getUserAllowsOwnerOrSelfButNotAnotherClientsAccount() {
		User existing = user(2, "maria@example.com", "maria");
		when(users.findById(2)).thenReturn(Optional.of(existing));

		assertEquals(2, new GetUser(users, transactions).execute(CLIENT, 2).id());
		assertEquals(2, new GetUser(users, transactions).execute(OWNER, 2).id());
		assertThrows(UserForbiddenException.class, () -> new GetUser(users, transactions).execute(CLIENT, 3));
		assertThrows(UserNotFoundException.class, () -> new GetUser(users, transactions).execute(OWNER, 3));
	}

	@Test
	void searchIsOwnerOnlyAndUsesBoundedStablePaginationDefaults() {
		when(users.searchByName("Maria", 0, 20))
				.thenReturn(new UserSearchPage(List.of(user(2, "maria@example.com", "maria")), 0, 20, 1));

		UserProfilePage page = new SearchUsers(users, transactions).execute(OWNER, "Maria", null, null);

		assertEquals(0, page.page());
		assertEquals(20, page.size());
		assertEquals(1, page.totalElements());
		assertThrows(UserForbiddenException.class, () -> new SearchUsers(users, transactions)
				.execute(CLIENT, "Maria", 0, 20));
		assertThrows(DomainValidationException.class, () -> new SearchUsers(users, transactions)
				.execute(OWNER, "Maria", 0, 101));
		assertThrows(DomainValidationException.class, () -> new SearchUsers(users, transactions)
				.execute(OWNER, " ", 0, 20));
	}

	@Test
	void deleteAllowsOwnerOrSelfAndRejectsOtherUsersAndMissingUsers() {
		when(users.findById(2)).thenReturn(Optional.of(user(2, "maria@example.com", "maria")));

		new DeleteUser(users, transactions).execute(CLIENT, 2);
		verify(users).deleteById(2);
		assertThrows(UserForbiddenException.class, () -> new DeleteUser(users, transactions).execute(CLIENT, 3));
		assertThrows(UserNotFoundException.class, () -> new DeleteUser(users, transactions).execute(OWNER, 3));
	}

	private static User user(long id, String email, String login) {
		return new User(
				id,
				"Maria",
				email,
				login,
				"$2a$hash",
				UserType.CLIENTE,
				LocalDateTime.of(2026, 11, 1, 10, 0),
				ADDRESS);
	}
}
