package br.com.fiap.techchallenge.usermanagement.adapter.configuration;

import java.time.Duration;
import java.util.Base64;

import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
		@NotBlank String secretBase64,
		@NotNull @DefaultValue("15m") Duration validity
) {

	public JwtProperties {
		if (secretBase64 != null && !secretBase64.isBlank()) {
			byte[] key;
			try {
				key = Base64.getDecoder().decode(secretBase64);
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException("app.security.jwt.secret-base64 must be valid Base64", exception);
			}
			if (key.length < 32) {
				throw new IllegalArgumentException("app.security.jwt.secret-base64 must contain at least 256 bits");
			}
		}
		if (validity != null && (validity.isZero() || validity.isNegative())) {
			throw new IllegalArgumentException("app.security.jwt.validity must be positive");
		}
	}

	public byte[] decodedSecret() {
		return Base64.getDecoder().decode(secretBase64);
	}
}
