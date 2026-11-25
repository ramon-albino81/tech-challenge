package br.com.fiap.techchallenge.usermanagement.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import br.com.fiap.techchallenge.usermanagement.application.exception.DuplicateUserAttribute;
import br.com.fiap.techchallenge.usermanagement.application.exception.UserConflictException;
import br.com.fiap.techchallenge.usermanagement.application.port.out.UserStore;
import br.com.fiap.techchallenge.usermanagement.domain.Address;
import br.com.fiap.techchallenge.usermanagement.domain.User;
import br.com.fiap.techchallenge.usermanagement.domain.UserType;

@SpringBootTest
@Testcontainers
class JpaUserStoreIntegrationTest {

	@Container
	static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4.4")
			.withDatabaseName("tech_challenge")
			.withUsername("tech_app")
			.withPassword("tech_password");

	@DynamicPropertySource
	static void databaseProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
		registry.add("spring.datasource.username", MYSQL::getUsername);
		registry.add("spring.datasource.password", MYSQL::getPassword);
		registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
		registry.add("spring.flyway.enabled", () -> "true");
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
	}

	@Autowired
	private UserStore users;

	@Test
	void migratesAndPersistsAddressJsonTimestampAndSearch() {
		User user = new User(
				null,
				"Maria Silva",
				"maria.integration@example.com",
				"maria.integration",
				"$2a$12$integration-hash",
				UserType.CLIENTE,
				LocalDateTime.of(2026, 11, 19, 15, 30, 0, 123456000),
				new Address("Rua Integração", "12A", "São Paulo", "01000-000"));

		User saved = users.save(user);
		User found = users.findById(saved.id().orElseThrow()).orElseThrow();

		assertEquals("maria.integration@example.com", found.email());
		assertEquals("Maria Silva", found.name());
		assertEquals(LocalDateTime.of(2026, 11, 19, 15, 30, 0, 123456000), found.lastModifiedAt());
		assertEquals(new Address("Rua Integração", "12A", "São Paulo", "01000-000"), found.address());
		assertEquals(1, users.searchByName("Maria", 0, 20).totalElements());

		users.deleteById(saved.id().orElseThrow());
		assertTrue(users.findById(saved.id().orElseThrow()).isEmpty());
	}

	@Test
	void rejectsConcurrentDuplicateEmailAndKeepsOneAccount() throws Exception {
		Callable<User> saveUser = () -> users.save(new User(
				null,
				"Concorrente",
				"concurrent@example.com",
				"concurrent-" + System.nanoTime(),
				"$2a$12$integration-hash",
				UserType.CLIENTE,
				LocalDateTime.of(2026, 11, 19, 17, 0),
				new Address("Rua", "1", "Cidade", "12345-678")));
		ExecutorService executor = Executors.newFixedThreadPool(2);
		Future<User> first = executor.submit(saveUser);
		Future<User> second = executor.submit(saveUser);

		int successCount = 0;
		for (Future<?> future : new Future<?>[] { first, second }) {
			try {
				future.get();
				successCount++;
			} catch (java.util.concurrent.ExecutionException exception) {
				assertTrue(exception.getCause() instanceof RuntimeException);
			}
		}
		executor.shutdownNow();

		assertEquals(1, successCount);
	}

	@Test
	void rejectsDuplicateEmailAndLoginWithDatabaseConstraints() {
		User first = new User(
				null,
				"João",
				"duplicate@example.com",
				"duplicate.login",
				"$2a$12$integration-hash",
				UserType.DONO_RESTAURANTE,
				LocalDateTime.of(2026, 11, 19, 16, 0),
				new Address("Rua", "1", "Cidade", "12345-678"));
		users.save(first);

		User duplicateEmail = new User(
				null,
				"Outra",
				"DUPLICATE@EXAMPLE.COM",
				"different.login",
				"$2a$12$integration-hash",
				UserType.CLIENTE,
				LocalDateTime.of(2026, 11, 19, 16, 1),
				new Address("Rua", "2", "Cidade", "12345-678"));
		UserConflictException emailConflict = assertThrows(UserConflictException.class, () -> users.save(duplicateEmail));
		assertEquals(DuplicateUserAttribute.EMAIL, emailConflict.attribute());

		User duplicateLogin = new User(
				null,
				"Outra",
				"another@example.com",
				"DUPLICATE.LOGIN",
				"$2a$12$integration-hash",
				UserType.CLIENTE,
				LocalDateTime.of(2026, 11, 19, 16, 2),
				new Address("Rua", "3", "Cidade", "12345-678"));
		UserConflictException loginConflict = assertThrows(UserConflictException.class, () -> users.save(duplicateLogin));
		assertEquals(DuplicateUserAttribute.LOGIN, loginConflict.attribute());
	}
}
