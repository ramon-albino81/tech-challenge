package br.com.fiap.techchallenge.usermanagement.adapter.in.web.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.fiap.techchallenge.usermanagement.domain.Address;

public record AddressResponse(
		String street,
		String number,
		String city,
		@JsonProperty("postal_code") String postalCode
) {

	static AddressResponse from(Address address) {
		return new AddressResponse(address.street(), address.number(), address.city(), address.postalCode());
	}
}
