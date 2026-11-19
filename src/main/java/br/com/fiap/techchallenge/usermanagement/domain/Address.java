package br.com.fiap.techchallenge.usermanagement.domain;

public record Address(String street, String number, String city, String postalCode) {

	public Address {
		street = required("street", street);
		number = required("number", number);
		city = required("city", city);
		postalCode = required("postalCode", postalCode);
	}

	private static String required(String field, String value) {
		if (value == null || value.isBlank()) {
			throw new DomainValidationException(field + " is required");
		}
		return value.strip();
	}
}
