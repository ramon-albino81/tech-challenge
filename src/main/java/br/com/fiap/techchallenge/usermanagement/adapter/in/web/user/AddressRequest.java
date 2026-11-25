package br.com.fiap.techchallenge.usermanagement.adapter.in.web.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.fiap.techchallenge.usermanagement.domain.Address;
import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
		@NotBlank String street,
		@NotBlank String number,
		@NotBlank String city,
		@NotBlank @JsonProperty("postal_code") String postalCode
) {

	Address toDomain() {
		return new Address(street, number, city, postalCode);
	}
}
