package br.com.fiap.techchallenge.usermanagement.adapter.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "app.seed.evaluation")
public record EvaluationSeedProperties(
		@NotBlank String name,
		@NotBlank @Email String email,
		@NotBlank String login,
		@NotBlank String password
) {
}
