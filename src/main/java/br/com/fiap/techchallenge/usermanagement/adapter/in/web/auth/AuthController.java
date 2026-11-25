package br.com.fiap.techchallenge.usermanagement.adapter.in.web.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.techchallenge.usermanagement.application.AuthenticateUser;
import br.com.fiap.techchallenge.usermanagement.application.exception.InvalidCredentialsException;
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
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Credenciais válidas"),
			@ApiResponse(responseCode = "401", description = "Credenciais inválidas")
	})
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		var token = authenticateUser.execute(request.login(), request.password());
		return new LoginResponse(token.value(), "Bearer", token.expiresAt());
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ProblemDetail> invalidCredentials() {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.UNAUTHORIZED,
				"Login ou senha inválidos.");
		problem.setTitle("Não autenticado");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
	}
}
