package br.com.fiap.techchallenge.usermanagement.adapter.out.persistence;

import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import br.com.fiap.techchallenge.usermanagement.application.exception.DuplicateUserAttribute;
import br.com.fiap.techchallenge.usermanagement.application.exception.UserConflictException;
import br.com.fiap.techchallenge.usermanagement.application.model.UserSearchPage;
import br.com.fiap.techchallenge.usermanagement.application.port.out.UserStore;
import br.com.fiap.techchallenge.usermanagement.domain.User;

@Repository
public class JpaUserStore implements UserStore {

	private final SpringDataUserRepository repository;

	JpaUserStore(SpringDataUserRepository repository) {
		this.repository = repository;
	}

	@Override
	public User save(User user) {
		try {
			return repository.saveAndFlush(UserJpaEntity.from(user)).toDomain();
		} catch (DataIntegrityViolationException exception) {
			String constraintName = constraintName(exception);
			if ("uk_app_user_email".equalsIgnoreCase(constraintName)) {
				throw new UserConflictException(DuplicateUserAttribute.EMAIL);
			}
			if ("uk_app_user_login".equalsIgnoreCase(constraintName)) {
				throw new UserConflictException(DuplicateUserAttribute.LOGIN);
			}
			throw exception;
		}
	}

	@Override
	public Optional<User> findById(long id) {
		return repository.findById(id).map(UserJpaEntity::toDomain);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return repository.findByEmail(email).map(UserJpaEntity::toDomain);
	}

	@Override
	public Optional<User> findByLogin(String login) {
		return repository.findByLogin(login).map(UserJpaEntity::toDomain);
	}

	@Override
	public UserSearchPage searchByName(String name, int page, int size) {
		var result = repository.findByNameContaining(name, PageRequest.of(page, size, Sort.by("id").ascending()));
		return new UserSearchPage(
				result.getContent().stream().map(UserJpaEntity::toDomain).toList(),
				result.getNumber(),
				result.getSize(),
				result.getTotalElements());
	}

	@Override
	public void deleteById(long id) {
		repository.deleteById(id);
		repository.flush();
	}

	private static String constraintName(Throwable exception) {
		Throwable current = exception;
		while (current != null) {
			if (current instanceof ConstraintViolationException violation
					&& violation.getConstraintName() != null) {
				return violation.getConstraintName();
			}
			String message = current.getMessage();
			if (message != null) {
				String lowerMessage = message.toLowerCase();
				if (lowerMessage.contains("uk_app_user_login")) {
					return "uk_app_user_login";
				}
				if (lowerMessage.contains("uk_app_user_email")) {
					return "uk_app_user_email";
				}
			}
			current = current.getCause();
		}
		return null;
	}
}
