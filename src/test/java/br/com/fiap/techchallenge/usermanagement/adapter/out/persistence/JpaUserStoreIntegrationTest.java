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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.ApplicationRunner;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import br.com.fiap.techchallenge.usermanagement.application.exception.DuplicateUserAttribute;
import br.com.fiap.techchallenge.usermanagement.application.exception.UserConflictException;
import br.com.fiap.techchallenge.usermanagement.application.port.out.PasswordHasher;
import br.com.fiap.techchallenge.usermanagement.application.port.out.UserStore;
import br.com.fiap.techchallenge.usermanagement.adapter.in.web.security.AuthenticatedUserActor;
import br.com.fiap.techchallenge.usermanagement.domain.Address;
import br.com.fiap.techchallenge.usermanagement.domain.User;
import br.com.fiap.techchallenge.usermanagement.domain.UserType;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("evaluation")
class JpaUserStoreIntegrationTest {

	private static final String JWT_SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

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
		registry.add("app.security.jwt.secret-base64", () -> JWT_SECRET);
		registry.add("app.seed.evaluation.name", () -> "Avaliador de Integração");
		registry.add("app.seed.evaluation.email", () -> "avaliador.integration@example.invalid");
		registry.add("app.seed.evaluation.login", () -> "avaliador.integration");
		registry.add("app.seed.evaluation.password", () -> "AvaliacaoLocal!2026");
	}

	@Autowired
	private UserStore users;

	@Autowired
	private PasswordHasher passwordHasher;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtDecoder jwtDecoder;

	@Autowired
	private JwtEncoder jwtEncoder;

	@Autowired
	private JwtAuthenticationConverter jwtAuthenticationConverter;

	@Autowired
	private AuthenticatedUserActor authenticatedUserActor;

	@Autowired
	private ApplicationRunner evaluationUserSeed;

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

	@Test
	void logsInWithValidCredentialsAndIssuesFifteenMinuteJwt() throws Exception {
		User user = users.save(new User(
				null,
				"Avaliador JWT",
				"jwt.integration@example.com",
				"jwt.integration",
				passwordHasher.hash("AvaliacaoLocal!2026"),
				UserType.DONO_RESTAURANTE,
				LocalDateTime.now(),
				new Address("Rua", "1", "Cidade", "01000-000")));

		var result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"login":"JWT.INTEGRATION","password":"AvaliacaoLocal!2026"}
								"""))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.tokenType")
						.value("Bearer"))
				.andReturn();
		String token = com.jayway.jsonpath.JsonPath.read(
				result.getResponse().getContentAsString(),
				"$.accessToken");

		var claims = jwtDecoder.decode(token);
		assertEquals(Long.toString(user.id().orElseThrow()), claims.getSubject());
		assertEquals("DONO_RESTAURANTE", claims.getClaimAsString("user_type"));
		assertEquals(900, claims.getExpiresAt().getEpochSecond() - claims.getIssuedAt().getEpochSecond());
		assertTrue(jwtAuthenticationConverter.convert(claims).getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_DONO_RESTAURANTE")));
		assertEquals(
				user.id().orElseThrow(),
				authenticatedUserActor.from(new JwtAuthenticationToken(claims)).userId());
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.get("/api/v1/users/9999")
						.header("Authorization", "Bearer " + token))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound());
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.post("/api/v1/users")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound());
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.get("/api/v1/users")
						.param("name", "Maria")
						.header("Authorization", "Bearer " + token))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	void seedsEvaluationOwnerIdempotentlyWithBcryptHashInMySql() throws Exception {
		User seeded = users.findByLogin("avaliador.integration").orElseThrow();
		String originalHash = seeded.passwordHash();
		assertEquals(UserType.DONO_RESTAURANTE, seeded.type());
		assertTrue(passwordHasher.matches("AvaliacaoLocal!2026", originalHash));

		evaluationUserSeed.run(null);

		User stillSeeded = users.findByLogin("avaliador.integration").orElseThrow();
		assertEquals(seeded.id(), stillSeeded.id());
		assertEquals(originalHash, stillSeeded.passwordHash());
	}

	@Test
	void exposesOpenApiInEvaluationProfile() throws Exception {
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/v3/api-docs"))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath(
						"$.paths['/api/v1/auth/login'].post").exists());
	}

	@Test
	void rejectsMissingAndInvalidBearerTokensWithProblemDetail() throws Exception {
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/users/1"))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
						.contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.detail")
						.value("Autenticação necessária ou token inválido."));

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/users/1")
						.header("Authorization", "Bearer malformed-token"))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
						.contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.post("/api/v1/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
	}

	@Test
	void rejectsExpiredAndIncorrectlySignedTokens() throws Exception {
		String expiredToken = jwtEncoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(),
				JwtClaimsSet.builder()
						.issuer("tech-challenge")
						.subject("1")
						.issuedAt(java.time.Instant.now().minusSeconds(1200))
						.expiresAt(java.time.Instant.now().minusSeconds(300))
						.claim("user_type", "DONO_RESTAURANTE")
						.build())).getTokenValue();

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/users/1")
						.header("Authorization", "Bearer " + expiredToken))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());

		User user = users.save(new User(
				null,
				"Outro",
				"wrong.signature@example.com",
				"wrong.signature",
				passwordHasher.hash("AvaliacaoLocal!2026"),
				UserType.CLIENTE,
				LocalDateTime.now(),
				new Address("Rua", "2", "Cidade", "01000-000")));
		String wrongSignature = jwtEncoderForDifferentSecret()
				.encode(JwtEncoderParameters.from(
						JwsHeader.with(MacAlgorithm.HS256).build(),
						JwtClaimsSet.builder()
								.issuer("tech-challenge")
								.subject(Long.toString(user.id().orElseThrow()))
								.issuedAt(java.time.Instant.now())
								.expiresAt(java.time.Instant.now().plusSeconds(900))
								.claim("user_type", "CLIENTE")
								.build()))
				.getTokenValue();

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/users/1")
						.header("Authorization", "Bearer " + wrongSignature))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
	}

	@Test
	void rejectsTokensWithoutValidIdentityAndUserTypeClaims() throws Exception {
		String token = jwtEncoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(),
				JwtClaimsSet.builder()
						.issuer("tech-challenge")
						.subject("not-a-user-id")
						.issuedAt(java.time.Instant.now())
						.expiresAt(java.time.Instant.now().plusSeconds(900))
						.claim("user_type", "ADMIN")
						.build())).getTokenValue();

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/users/1")
						.header("Authorization", "Bearer " + token))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
	}

	@Test
	void rejectsInvalidCredentialsGenerically() throws Exception {
		users.save(new User(
				null,
				"Login inválido",
				"invalid.login@example.com",
				"invalid.login",
				passwordHasher.hash("senha-correta"),
				UserType.CLIENTE,
				LocalDateTime.now(),
				new Address("Rua", "3", "Cidade", "01000-000")));

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"login":"invalid.login","password":"senha-incorreta"}
								"""))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
						.contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.detail")
						.value("Login ou senha inválidos."));

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"login":"desconhecido","password":"senha-incorreta"}
								"""))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.detail")
						.value("Login ou senha inválidos."));
	}

	@Test
	void logsInClientWithClientAuthority() throws Exception {
		User user = users.save(new User(
				null,
				"Cliente JWT",
				"client.jwt@example.com",
				"client.jwt",
				passwordHasher.hash("AvaliacaoLocal!2026"),
				UserType.CLIENTE,
				LocalDateTime.now(),
				new Address("Rua", "4", "Cidade", "01000-000")));

		var result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"login":"client.jwt","password":"AvaliacaoLocal!2026"}
								"""))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
				.andReturn();
		String token = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
		var claims = jwtDecoder.decode(token);

		assertEquals(Long.toString(user.id().orElseThrow()), claims.getSubject());
		assertTrue(jwtAuthenticationConverter.convert(claims).getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_CLIENTE")));
		assertEquals(UserType.CLIENTE, authenticatedUserActor.from(new JwtAuthenticationToken(claims)).type());
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.post("/api/v1/users")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
						.contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.get("/api/v1/users")
						.param("name", "Maria")
						.header("Authorization", "Bearer " + token))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
	}

	private org.springframework.security.oauth2.jwt.JwtEncoder jwtEncoderForDifferentSecret() {
		byte[] secret = "different-secret-with-at-least-32-bytes".getBytes(java.nio.charset.StandardCharsets.UTF_8);
		return new org.springframework.security.oauth2.jwt.NimbusJwtEncoder(
				new com.nimbusds.jose.jwk.source.ImmutableSecret<>(
						new javax.crypto.spec.SecretKeySpec(secret, "HmacSHA256")));
	}
}
