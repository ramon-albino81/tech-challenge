package br.com.fiap.techchallenge.usermanagement.adapter.in.web.user;

import java.util.List;

import br.com.fiap.techchallenge.usermanagement.application.model.UserProfilePage;

public record UserProfilePageResponse(
		List<UserProfileResponse> users,
		int page,
		int size,
		long totalElements
) {

	static UserProfilePageResponse from(UserProfilePage page) {
		return new UserProfilePageResponse(
				page.users().stream().map(UserProfileResponse::from).toList(),
				page.page(),
				page.size(),
				page.totalElements());
	}
}
