package br.com.fiap.techchallenge.usermanagement.application.model;

import java.util.List;

public record UserProfilePage(List<UserProfile> users, int page, int size, long totalElements) {

	public UserProfilePage {
		users = List.copyOf(users);
	}
}
