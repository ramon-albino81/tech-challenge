package br.com.fiap.techchallenge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;

import br.com.fiap.techchallenge.usermanagement.adapter.out.persistence.JpaUserStore;

@SpringBootTest(properties = {
	"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
		"org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class TechChallengeApplicationTests {

	@MockitoBean
	private JpaUserStore jpaUserStore;

	@MockitoBean
	private PlatformTransactionManager transactionManager;

	@Test
	void contextLoads() {
	}
}
