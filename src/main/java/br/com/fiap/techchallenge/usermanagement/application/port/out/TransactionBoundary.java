package br.com.fiap.techchallenge.usermanagement.application.port.out;

import java.util.function.Supplier;

public interface TransactionBoundary {

	<T> T required(Supplier<T> operation);

	<T> T readOnly(Supplier<T> operation);
}
