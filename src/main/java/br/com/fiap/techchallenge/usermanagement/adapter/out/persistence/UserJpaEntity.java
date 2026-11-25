package br.com.fiap.techchallenge.usermanagement.adapter.out.persistence;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import br.com.fiap.techchallenge.usermanagement.domain.User;
import br.com.fiap.techchallenge.usermanagement.domain.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class UserJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", columnDefinition = "BIGINT UNSIGNED")
	private Long id;

	@Column(name = "name", nullable = false, length = 150)
	private String name;

	@Column(name = "email", nullable = false, length = 254)
	private String email;

	@Column(name = "login", nullable = false, length = 100)
	private String login;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(name = "user_type", nullable = false, length = 32)
	private UserType type;

	@Column(name = "last_modified_at", nullable = false, columnDefinition = "datetime(6)")
	private LocalDateTime lastModifiedAt;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "address", nullable = false, columnDefinition = "json")
	private AddressDocument address;

	protected UserJpaEntity() {
	}

	static UserJpaEntity from(User user) {
		UserJpaEntity entity = new UserJpaEntity();
		entity.id = user.id().isPresent() ? user.id().getAsLong() : null;
		entity.name = user.name();
		entity.email = user.email();
		entity.login = user.login();
		entity.passwordHash = user.passwordHash();
		entity.type = user.type();
		entity.lastModifiedAt = user.lastModifiedAt();
		entity.address = AddressDocument.from(user.address());
		return entity;
	}

	User toDomain() {
		return new User(id, name, email, login, passwordHash, type, lastModifiedAt, address.toDomain());
	}
}
