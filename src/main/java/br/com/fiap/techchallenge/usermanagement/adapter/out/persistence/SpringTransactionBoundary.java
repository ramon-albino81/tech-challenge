package br.com.fiap.techchallenge.usermanagement.adapter.out.persistence;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import br.com.fiap.techchallenge.usermanagement.application.port.out.TransactionBoundary;

@Component
class SpringTransactionBoundary implements TransactionBoundary {

	private final TransactionTemplate required;
	private final TransactionTemplate readOnly;

	SpringTransactionBoundary(PlatformTransactionManager transactionManager) {
		this.required = new TransactionTemplate(transactionManager);
		this.readOnly = new TransactionTemplate(transactionManager);
		this.readOnly.setReadOnly(true);
	}

	@Override
	public <T> T required(Supplier<T> operation) {
		return required.execute(status -> operation.get());
	}

	@Override
	public <T> T readOnly(Supplier<T> operation) {
		return readOnly.execute(status -> operation.get());
	}
}
