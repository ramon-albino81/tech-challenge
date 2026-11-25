package br.com.fiap.techchallenge.usermanagement.adapter.configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import br.com.fiap.techchallenge.usermanagement.adapter.out.security.ProblemDetailWriter;
import br.com.fiap.techchallenge.usermanagement.domain.DomainValidationException;
import br.com.fiap.techchallenge.usermanagement.domain.UserType;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(JwtProperties.class)
class SecurityConfiguration {

	private static final String ISSUER = "tech-challenge";

	@Bean
	SecretKey jwtSecretKey(JwtProperties properties) {
		return new SecretKeySpec(properties.decodedSecret(), "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey secretKey) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
	}

	@Bean
	JwtDecoder jwtDecoder(SecretKey secretKey) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
		OAuth2TokenValidator<Jwt> requiredClaims = jwt -> {
			try {
				if (Long.parseLong(jwt.getSubject()) <= 0) {
					return invalidClaims();
				}
				UserType.fromValue(jwt.getClaimAsString("user_type"));
				return OAuth2TokenValidatorResult.success();
			} catch (NumberFormatException | DomainValidationException exception) {
				return invalidClaims();
			}
		};
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
				JwtValidators.createDefaultWithIssuer(ISSUER),
				requiredClaims));
		return decoder;
	}

	private static OAuth2TokenValidatorResult invalidClaims() {
		return OAuth2TokenValidatorResult.failure(
				new OAuth2Error("invalid_token", "Required JWT claims are invalid", null));
	}

	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
		authorities.setAuthoritiesClaimName("user_type");
		authorities.setAuthorityPrefix("ROLE_");
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(authorities);
		return converter;
	}

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationConverter jwtAuthenticationConverter,
			ProblemDetailWriter problemDetailWriter,
			Environment environment
	) throws Exception {
		AuthenticationEntryPoint authenticationEntryPoint = (request, response, exception) ->
				problemDetailWriter.write(response, HttpStatus.UNAUTHORIZED, "Não autenticado",
						"Autenticação necessária ou token inválido.");
		AccessDeniedHandler accessDeniedHandler = (request, response, exception) ->
				problemDetailWriter.write(response, HttpStatus.FORBIDDEN, "Acesso negado",
						"Você não tem permissão para executar esta operação.");

		return http
				.csrf(csrf -> csrf.disable())
				.formLogin(form -> form.disable())
				.httpBasic(basic -> basic.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(errors -> errors
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler))
				.authorizeHttpRequests(authorize -> {
					authorize.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll();
					authorize.requestMatchers(HttpMethod.POST, "/api/v1/users")
							.hasRole(UserType.DONO_RESTAURANTE.name());
					authorize.requestMatchers(HttpMethod.GET, "/api/v1/users")
							.hasRole(UserType.DONO_RESTAURANTE.name());
					if (environment.acceptsProfiles(Profiles.of("prod"))) {
						authorize.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
								.authenticated();
					} else {
						authorize.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
								.permitAll();
					}
					authorize.requestMatchers("/api/v1/**").authenticated();
					authorize.anyRequest().denyAll();
				})
				.oauth2ResourceServer(resourceServer -> resourceServer
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler))
				.build();
	}

}
