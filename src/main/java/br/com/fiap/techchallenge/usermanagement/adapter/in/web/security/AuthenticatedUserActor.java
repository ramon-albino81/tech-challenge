package br.com.fiap.techchallenge.usermanagement.adapter.in.web.security;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import br.com.fiap.techchallenge.usermanagement.application.model.UserActor;
import br.com.fiap.techchallenge.usermanagement.domain.UserType;

@Component
public class AuthenticatedUserActor {

	public UserActor from(JwtAuthenticationToken authentication) {
		return new UserActor(
				Long.parseLong(authentication.getToken().getSubject()),
				UserType.fromValue(authentication.getToken().getClaimAsString("user_type")));
	}
}
