package br.com.fiap.techchallenge.usermanagement.adapter.configuration;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(info = @Info(
		title = "Tech Challenge FIAP - API de usuários",
		version = "v1",
		description = "API de autenticação e gerenciamento de usuários. Endpoints protegidos requerem JWT Bearer."))
@SecurityScheme(
		name = "bearerAuth",
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "JWT",
		in = SecuritySchemeIn.HEADER)
class OpenApiConfiguration {
}
