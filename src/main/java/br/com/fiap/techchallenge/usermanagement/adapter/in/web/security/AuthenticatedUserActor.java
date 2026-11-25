package br.com.fiap.techchallenge.usermanagement.adapter.in.web.security;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import br.com.fiap.techchallenge.usermanagement.application.model.UserActor;
import br.com.fiap.techchallenge.usermanagement.domain.UserType;

@Component
public class AuthenticatedUserActor {

	public UserActor from(JwtAuthenticationToken authentication) {
		return from(authentication.getToken());
	}

	public UserActor from(Jwt jwt) {
		return new UserActor(
				Long.parseLong(jwt.getSubject()),
				UserType.fromValue(jwt.getClaimAsString("user_type")));
	}
}
