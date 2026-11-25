package br.com.fiap.techchallenge.usermanagement.adapter.configuration;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.fiap.techchallenge.usermanagement.application.ChangeOwnPassword;
import br.com.fiap.techchallenge.usermanagement.application.CreateUser;
import br.com.fiap.techchallenge.usermanagement.application.DeleteUser;
import br.com.fiap.techchallenge.usermanagement.application.GetUser;
import br.com.fiap.techchallenge.usermanagement.application.SearchUsers;
import br.com.fiap.techchallenge.usermanagement.application.UpdateUserProfile;
import br.com.fiap.techchallenge.usermanagement.application.port.out.PasswordHasher;
import br.com.fiap.techchallenge.usermanagement.application.port.out.TransactionBoundary;
import br.com.fiap.techchallenge.usermanagement.application.port.out.UserStore;

@Configuration(proxyBeanMethods = false)
class UserManagementConfiguration {

	@Bean
	Clock userManagementClock() {
		return Clock.systemUTC();
	}

	@Bean
	CreateUser createUser(UserStore users, PasswordHasher passwordHasher, TransactionBoundary transactions, Clock clock) {
		return new CreateUser(users, passwordHasher, transactions, clock);
	}

	@Bean
	UpdateUserProfile updateUserProfile(UserStore users, TransactionBoundary transactions, Clock clock) {
		return new UpdateUserProfile(users, transactions, clock);
	}

	@Bean
	ChangeOwnPassword changeOwnPassword(
			UserStore users,
			PasswordHasher passwordHasher,
			TransactionBoundary transactions,
			Clock clock
	) {
		return new ChangeOwnPassword(users, passwordHasher, transactions, clock);
	}

	@Bean
	GetUser getUser(UserStore users, TransactionBoundary transactions) {
		return new GetUser(users, transactions);
	}

	@Bean
	SearchUsers searchUsers(UserStore users, TransactionBoundary transactions) {
		return new SearchUsers(users, transactions);
	}

	@Bean
	DeleteUser deleteUser(UserStore users, TransactionBoundary transactions) {
		return new DeleteUser(users, transactions);
	}
}
