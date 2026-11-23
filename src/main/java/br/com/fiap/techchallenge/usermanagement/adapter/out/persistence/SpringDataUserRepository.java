package br.com.fiap.techchallenge.usermanagement.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {

	Optional<UserJpaEntity> findByEmail(String email);

	Optional<UserJpaEntity> findByLogin(String login);

	Page<UserJpaEntity> findByNameContaining(String name, Pageable pageable);
}
