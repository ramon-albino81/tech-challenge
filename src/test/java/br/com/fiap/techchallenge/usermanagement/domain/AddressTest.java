package br.com.fiap.techchallenge.usermanagement.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AddressTest {

	@Test
	void trimsAddressFields() {
		Address address = new Address(" Rua A ", " 12 ", " São Paulo ", " 01000-000 ");

		assertEquals("Rua A", address.street());
		assertEquals("12", address.number());
		assertEquals("São Paulo", address.city());
		assertEquals("01000-000", address.postalCode());
	}

	@Test
	void rejectsMissingAddressFields() {
		assertThrows(DomainValidationException.class, () -> new Address(null, "12", "Cidade", "01000-000"));
		assertThrows(DomainValidationException.class, () -> new Address("Rua", " ", "Cidade", "01000-000"));
		assertThrows(DomainValidationException.class, () -> new Address("Rua", "12", null, "01000-000"));
		assertThrows(DomainValidationException.class, () -> new Address("Rua", "12", "Cidade", ""));
	}
}
