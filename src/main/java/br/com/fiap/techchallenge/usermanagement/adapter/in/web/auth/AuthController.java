package br.com.fiap.techchallenge.usermanagement.adapter.in.web.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.techchallenge.usermanagement.application.AuthenticateUser;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação")
public class AuthController {

	private final AuthenticateUser authenticateUser;

	public AuthController(AuthenticateUser authenticateUser) {
		this.authenticateUser = authenticateUser;
	}

	@PostMapping("/login")
	@Operation(summary = "Autenticar usuário e emitir token JWT")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(
			mediaType = "application/json",
			schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = LoginRequest.class),
			examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
					value = "{\"login\":\"avaliador\",\"password\":\"AvaliacaoLocal!2026\"}")))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Credenciais válidas", content = @io.swagger.v3.oas.annotations.media.Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = LoginResponse.class),
					examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
							value = "{\"accessToken\":\"<jwt>\",\"tokenType\":\"Bearer\",\"expiresAt\":\"2026-01-01T10:15:00Z\"}"))),
			@ApiResponse(responseCode = "400", description = "Dados de login inválidos", content = @io.swagger.v3.oas.annotations.media.Content(
					mediaType = "application/problem+json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
							value = "{\"type\":\"about:blank\",\"title\":\"Requisição inválida\",\"status\":400,\"detail\":\"Um ou mais campos não atendem aos critérios de validação.\"}"))),
			@ApiResponse(responseCode = "401", description = "Credenciais inválidas", content = @io.swagger.v3.oas.annotations.media.Content(
					mediaType = "application/problem+json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
							value = "{\"type\":\"about:blank\",\"title\":\"Não autenticado\",\"status\":401,\"detail\":\"Login ou senha inválidos.\"}"))),
			@ApiResponse(responseCode = "500", description = "Falha interna", content = @io.swagger.v3.oas.annotations.media.Content(
					mediaType = "application/problem+json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
							value = "{\"type\":\"about:blank\",\"title\":\"Erro interno\",\"status\":500,\"detail\":\"Não foi possível concluir a solicitação.\"}")))
	})
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		var token = authenticateUser.execute(request.login(), request.password());
		return new LoginResponse(token.value(), "Bearer", token.expiresAt());
	}
}
