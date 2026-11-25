package br.com.fiap.techchallenge.usermanagement.application.port.out;

import java.util.Optional;

import br.com.fiap.techchallenge.usermanagement.application.model.UserSearchPage;
import br.com.fiap.techchallenge.usermanagement.domain.User;

public interface UserStore {

	User save(User user);

	Optional<User> findById(long id);

	Optional<User> findByEmail(String email);

	Optional<User> findByLogin(String login);

	UserSearchPage searchByName(String name, int page, int size);

	void deleteById(long id);
}
