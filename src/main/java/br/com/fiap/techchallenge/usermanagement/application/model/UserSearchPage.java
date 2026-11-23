package br.com.fiap.techchallenge.usermanagement.application.model;

import java.util.List;

import br.com.fiap.techchallenge.usermanagement.domain.User;

public record UserSearchPage(List<User> users, int page, int size, long totalElements) {

	public UserSearchPage {
		users = List.copyOf(users);
	}
}
