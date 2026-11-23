package br.com.fiap.techchallenge.usermanagement.adapter.out.persistence;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.fiap.techchallenge.usermanagement.domain.Address;

public record AddressDocument(
		String street,
		String number,
		String city,
		@JsonProperty("postal_code") String postalCode
) {

	static AddressDocument from(Address address) {
		return new AddressDocument(address.street(), address.number(), address.city(), address.postalCode());
	}

	Address toDomain() {
		return new Address(street, number, city, postalCode);
	}
}
