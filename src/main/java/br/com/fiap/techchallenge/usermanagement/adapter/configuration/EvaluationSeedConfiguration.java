package br.com.fiap.techchallenge.usermanagement.adapter.configuration;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import br.com.fiap.techchallenge.usermanagement.application.port.out.PasswordHasher;
import br.com.fiap.techchallenge.usermanagement.application.port.out.UserStore;
import br.com.fiap.techchallenge.usermanagement.domain.Address;
import br.com.fiap.techchallenge.usermanagement.domain.User;
import br.com.fiap.techchallenge.usermanagement.domain.UserType;

@Configuration(proxyBeanMethods = false)
@Profile({"development", "evaluation"})
@EnableConfigurationProperties(EvaluationSeedProperties.class)
class EvaluationSeedConfiguration {

	@Bean
	ApplicationRunner evaluationUserSeed(
			UserStore users,
			PasswordHasher passwordHasher,
			EvaluationSeedProperties properties,
			Clock userManagementClock
	) {
		return arguments -> {
			if (users.findByLogin(properties.login().strip()).isEmpty()) {
				User evaluationUser = new User(
						null,
						properties.name(),
						properties.email(),
						properties.login(),
						passwordHasher.hash(properties.password()),
						UserType.DONO_RESTAURANTE,
						LocalDateTime.now(userManagementClock),
						new Address("Rua de Avaliação", "1", "São Paulo", "01000-000"));
				users.save(evaluationUser);
			}
		};
	}
}
