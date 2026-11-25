package br.com.fiap.techchallenge.usermanagement.adapter.out.security;

import java.time.Clock;
import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import br.com.fiap.techchallenge.usermanagement.adapter.configuration.JwtProperties;
import br.com.fiap.techchallenge.usermanagement.application.port.out.AccessTokenIssuer;
import br.com.fiap.techchallenge.usermanagement.domain.User;

@Component
class JwtAccessTokenIssuer implements AccessTokenIssuer {

	private static final String ISSUER = "tech-challenge";

	private final JwtEncoder encoder;
	private final JwtProperties properties;
	private final Clock clock;

	JwtAccessTokenIssuer(JwtEncoder encoder, JwtProperties properties, Clock clock) {
		this.encoder = encoder;
		this.properties = properties;
		this.clock = clock;
	}

	@Override
	public IssuedToken issue(User user) {
		Instant issuedAt = clock.instant();
		Instant expiresAt = issuedAt.plus(properties.validity());
		String subject = Long.toString(user.id().orElseThrow());
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(ISSUER)
				.subject(subject)
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.claim("user_type", user.type().name())
				.build();
		String token = encoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(),
				claims)).getTokenValue();
		return new IssuedToken(token, expiresAt);
	}
}
