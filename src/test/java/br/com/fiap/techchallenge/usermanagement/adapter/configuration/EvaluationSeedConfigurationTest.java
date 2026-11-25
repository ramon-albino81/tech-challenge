package br.com.fiap.techchallenge.usermanagement.adapter.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import br.com.fiap.techchallenge.usermanagement.application.port.out.PasswordHasher;
import br.com.fiap.techchallenge.usermanagement.application.port.out.UserStore;
import br.com.fiap.techchallenge.usermanagement.domain.User;
import br.com.fiap.techchallenge.usermanagement.domain.UserType;

class EvaluationSeedConfigurationTest {

	private final UserStore users = org.mockito.Mockito.mock(UserStore.class);
	private final PasswordHasher passwordHasher = org.mockito.Mockito.mock(PasswordHasher.class);
	private final Clock clock = Clock.fixed(Instant.parse("2026-11-24T21:00:00Z"), ZoneOffset.UTC);

	@Test
	void createsEvaluationOwnerOnceWithHashedPasswordInAuthorizedProfile() {
		AtomicReference<User> stored = new AtomicReference<>();
		when(users.findByLogin("avaliador")).thenAnswer(invocation -> Optional.ofNullable(stored.get()));
		when(passwordHasher.hash("AvaliacaoLocal!2026")).thenReturn("$2a$hash");
		when(users.save(any(User.class))).thenAnswer(invocation -> {
			User user = invocation.getArgument(0);
			stored.set(user.withId(10));
			return stored.get();
		});

		contextRunner("evaluation").run(context -> {
			ApplicationRunner seed = context.getBean(ApplicationRunner.class);
			seed.run(null);
			seed.run(null);

			org.mockito.ArgumentCaptor<User> saved = org.mockito.ArgumentCaptor.forClass(User.class);
			verify(users).save(saved.capture());
			assertEquals(UserType.DONO_RESTAURANTE, saved.getValue().type());
			assertEquals("$2a$hash", saved.getValue().passwordHash());
			assertEquals("avaliador@example.invalid", saved.getValue().email());
		});
	}

	@Test
	void doesNotReplaceAnExistingEvaluationLogin() {
		when(users.findByLogin("avaliador")).thenReturn(Optional.of(
				new User(
						7L,
						"Avaliador existente",
						"avaliador@example.invalid",
						"avaliador",
						"$2a$existing",
						UserType.DONO_RESTAURANTE,
						java.time.LocalDateTime.of(2026, 11, 1, 0, 0),
						new br.com.fiap.techchallenge.usermanagement.domain.Address("Rua", "1", "Cidade", "01000-000"))));

		contextRunner("evaluation").run(context ->
				context.getBean(ApplicationRunner.class).run(null));

		verify(users, never()).save(any(User.class));
		verify(passwordHasher, never()).hash(any());
	}

	@Test
	void doesNotRegisterSeedOutsideAuthorizedProfiles() {
		contextRunner("prod").run(context -> {
			org.junit.jupiter.api.Assertions.assertFalse(context.containsBean("evaluationUserSeed"));
			verify(users, never()).findByLogin(any());
		});
	}

	private ApplicationContextRunner contextRunner(String profile) {
		return new ApplicationContextRunner()
				.withUserConfiguration(EvaluationSeedConfiguration.class)
				.withBean(UserStore.class, () -> users)
				.withBean(PasswordHasher.class, () -> passwordHasher)
				.withBean(Clock.class, () -> clock)
				.withPropertyValues(
						"spring.profiles.active=" + profile,
						"app.seed.evaluation.name=Avaliador Local",
						"app.seed.evaluation.email=avaliador@example.invalid",
						"app.seed.evaluation.login=avaliador",
						"app.seed.evaluation.password=AvaliacaoLocal!2026");
	}
}
