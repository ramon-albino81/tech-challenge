package br.com.fiap.techchallenge.usermanagement.adapter.in.web.user;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.techchallenge.usermanagement.adapter.in.web.security.AuthenticatedUserActor;
import br.com.fiap.techchallenge.usermanagement.application.ChangeOwnPassword;
import br.com.fiap.techchallenge.usermanagement.application.CreateUser;
import br.com.fiap.techchallenge.usermanagement.application.DeleteUser;
import br.com.fiap.techchallenge.usermanagement.application.GetUser;
import br.com.fiap.techchallenge.usermanagement.application.SearchUsers;
import br.com.fiap.techchallenge.usermanagement.application.UpdateUserProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Usuários")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

	private final CreateUser createUser;
	private final GetUser getUser;
	private final SearchUsers searchUsers;
	private final UpdateUserProfile updateUserProfile;
	private final ChangeOwnPassword changeOwnPassword;
	private final DeleteUser deleteUser;
	private final AuthenticatedUserActor actors;

	public UserController(
			CreateUser createUser,
			GetUser getUser,
			SearchUsers searchUsers,
			UpdateUserProfile updateUserProfile,
			ChangeOwnPassword changeOwnPassword,
			DeleteUser deleteUser,
			AuthenticatedUserActor actors
	) {
		this.createUser = createUser;
		this.getUser = getUser;
		this.searchUsers = searchUsers;
		this.updateUserProfile = updateUserProfile;
		this.changeOwnPassword = changeOwnPassword;
		this.deleteUser = deleteUser;
		this.actors = actors;
	}

	@PostMapping
	@Operation(summary = "Criar usuário como DONO_RESTAURANTE")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = CreateUserRequest.class),
			examples = @ExampleObject(value = "{\"name\":\"Maria Silva\",\"email\":\"maria@example.com\",\"password\":\"SenhaLocal123!\",\"login\":\"maria\",\"type\":\"CLIENTE\",\"address\":{\"street\":\"Rua Exemplo\",\"number\":\"10\",\"city\":\"São Paulo\",\"postal_code\":\"01000-000\"}}")))
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Usuário criado", content = @Content(
					mediaType = "application/json", schema = @Schema(implementation = UserProfileResponse.class),
					examples = @ExampleObject(value = "{\"id\":1,\"name\":\"Maria Silva\",\"email\":\"maria@example.com\",\"login\":\"maria\",\"type\":\"CLIENTE\",\"lastModifiedAt\":\"2026-01-01T10:00:00\",\"address\":{\"street\":\"Rua Exemplo\",\"number\":\"10\",\"city\":\"São Paulo\",\"postal_code\":\"01000-000\"}}"))),
			@ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Requisição inválida\",\"status\":400,\"detail\":\"Um ou mais campos não atendem aos critérios de validação.\"}"))),
			@ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Não autenticado\",\"status\":401,\"detail\":\"Autenticação necessária ou token inválido.\"}"))),
			@ApiResponse(responseCode = "403", description = "Somente DONO_RESTAURANTE pode criar usuário", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Acesso negado\",\"status\":403,\"detail\":\"Você não tem permissão para executar esta operação.\"}"))),
			@ApiResponse(responseCode = "409", description = "E-mail ou login já utilizado", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Conflito de usuário\",\"status\":409,\"detail\":\"O e-mail informado já está em uso.\",\"attribute\":\"email\"}"))),
			@ApiResponse(responseCode = "500", description = "Falha interna", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Erro interno\",\"status\":500,\"detail\":\"Não foi possível concluir a solicitação.\"}")))
	})
	public ResponseEntity<UserProfileResponse> create(
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody CreateUserRequest request
	) {
		UserProfileResponse created = UserProfileResponse.from(
				createUser.execute(actors.from(jwt), request.toCommand()));
		return ResponseEntity.created(URI.create("/api/v1/users/" + created.id())).body(created);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar usuário por identificador")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Usuário encontrado", content = @Content(
					mediaType = "application/json", schema = @Schema(implementation = UserProfileResponse.class),
					examples = @ExampleObject(value = "{\"id\":1,\"name\":\"Maria Silva\",\"email\":\"maria@example.com\",\"login\":\"maria\",\"type\":\"CLIENTE\",\"lastModifiedAt\":\"2026-01-01T10:00:00\",\"address\":{\"street\":\"Rua Exemplo\",\"number\":\"10\",\"city\":\"São Paulo\",\"postal_code\":\"01000-000\"}}"))),
			@ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Não autenticado\",\"status\":401,\"detail\":\"Autenticação necessária ou token inválido.\"}"))),
			@ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Acesso negado\",\"status\":403,\"detail\":\"Você não tem permissão para executar esta operação.\"}"))),
			@ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Usuário não encontrado\",\"status\":404,\"detail\":\"O usuário solicitado não foi encontrado.\"}"))),
			@ApiResponse(responseCode = "500", description = "Falha interna", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Erro interno\",\"status\":500,\"detail\":\"Não foi possível concluir a solicitação.\"}")))
	})
	public UserProfileResponse get(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable @Positive long id
	) {
		return UserProfileResponse.from(getUser.execute(actors.from(jwt), id));
	}

	@GetMapping
	@Operation(summary = "Buscar usuários por nome; somente DONO_RESTAURANTE")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Página de usuários", content = @Content(
					mediaType = "application/json", schema = @Schema(implementation = UserProfilePageResponse.class),
					examples = @ExampleObject(value = "{\"users\":[{\"id\":1,\"name\":\"Maria Silva\",\"email\":\"maria@example.com\",\"login\":\"maria\",\"type\":\"CLIENTE\",\"lastModifiedAt\":\"2026-01-01T10:00:00\",\"address\":{\"street\":\"Rua Exemplo\",\"number\":\"10\",\"city\":\"São Paulo\",\"postal_code\":\"01000-000\"}}],\"page\":0,\"size\":20,\"totalElements\":1}"))),
			@ApiResponse(responseCode = "400", description = "Parâmetros inválidos", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Requisição inválida\",\"status\":400,\"detail\":\"Um ou mais parâmetros não atendem aos critérios de validação.\"}"))),
			@ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Não autenticado\",\"status\":401,\"detail\":\"Autenticação necessária ou token inválido.\"}"))),
			@ApiResponse(responseCode = "403", description = "Somente DONO_RESTAURANTE pode buscar", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Acesso negado\",\"status\":403,\"detail\":\"Você não tem permissão para executar esta operação.\"}"))),
			@ApiResponse(responseCode = "500", description = "Falha interna", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Erro interno\",\"status\":500,\"detail\":\"Não foi possível concluir a solicitação.\"}")))
	})
	public UserProfilePageResponse search(
			@AuthenticationPrincipal Jwt jwt,
			@RequestParam @NotBlank String name,
			@RequestParam(required = false, defaultValue = "0") @Min(0) Integer page,
			@RequestParam(required = false, defaultValue = "20") @Min(1) @Max(100) Integer size
	) {
		return UserProfilePageResponse.from(searchUsers.execute(actors.from(jwt), name, page, size));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Atualizar perfil sem alterar senha ou tipo")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = UpdateUserProfileRequest.class),
			examples = @ExampleObject(value = "{\"name\":\"Maria Silva\",\"email\":\"maria@example.com\",\"login\":\"maria\",\"address\":{\"street\":\"Rua Exemplo\",\"number\":\"10\",\"city\":\"São Paulo\",\"postal_code\":\"01000-000\"}}")))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Perfil atualizado", content = @Content(
					mediaType = "application/json", schema = @Schema(implementation = UserProfileResponse.class),
					examples = @ExampleObject(value = "{\"id\":1,\"name\":\"Maria Silva\",\"email\":\"maria@example.com\",\"login\":\"maria\",\"type\":\"CLIENTE\",\"lastModifiedAt\":\"2026-01-01T10:00:00\",\"address\":{\"street\":\"Rua Exemplo\",\"number\":\"10\",\"city\":\"São Paulo\",\"postal_code\":\"01000-000\"}}"))),
			@ApiResponse(responseCode = "400", description = "Dados inválidos ou campos não permitidos", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Requisição inválida\",\"status\":400,\"detail\":\"O corpo da requisição está ausente ou malformado, ou contém campos não suportados.\"}"))),
			@ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Não autenticado\",\"status\":401,\"detail\":\"Autenticação necessária ou token inválido.\"}"))),
			@ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Acesso negado\",\"status\":403,\"detail\":\"Você não tem permissão para executar esta operação.\"}"))),
			@ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Usuário não encontrado\",\"status\":404,\"detail\":\"O usuário solicitado não foi encontrado.\"}"))),
			@ApiResponse(responseCode = "409", description = "E-mail ou login já utilizado", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Conflito de usuário\",\"status\":409,\"detail\":\"O login informado já está em uso.\",\"attribute\":\"login\"}"))),
			@ApiResponse(responseCode = "500", description = "Falha interna", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Erro interno\",\"status\":500,\"detail\":\"Não foi possível concluir a solicitação.\"}")))
	})
	public UserProfileResponse update(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable @Positive long id,
			@Valid @RequestBody UpdateUserProfileRequest request
	) {
		return UserProfileResponse.from(
				updateUserProfile.execute(actors.from(jwt), request.toCommand(id)));
	}

	@PutMapping("/{id}/password")
	@Operation(summary = "Alterar a própria senha mediante confirmação da senha atual")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
			mediaType = "application/json",
			schema = @Schema(implementation = ChangePasswordRequest.class),
			examples = @ExampleObject(value = "{\"currentPassword\":\"SenhaAtual123!\",\"newPassword\":\"NovaSenha123!\"}")))
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Senha alterada"),
			@ApiResponse(responseCode = "400", description = "Senha inválida ou senha atual incorreta", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Requisição inválida\",\"status\":400,\"detail\":\"A senha atual não confere.\"}"))),
			@ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Não autenticado\",\"status\":401,\"detail\":\"Autenticação necessária ou token inválido.\"}"))),
			@ApiResponse(responseCode = "403", description = "Não é permitido alterar senha de outra conta", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Acesso negado\",\"status\":403,\"detail\":\"Você não tem permissão para executar esta operação.\"}"))),
			@ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Usuário não encontrado\",\"status\":404,\"detail\":\"O usuário solicitado não foi encontrado.\"}"))),
			@ApiResponse(responseCode = "500", description = "Falha interna", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Erro interno\",\"status\":500,\"detail\":\"Não foi possível concluir a solicitação.\"}")))
	})
	public ResponseEntity<Void> changePassword(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable @Positive long id,
			@Valid @RequestBody ChangePasswordRequest request
	) {
		changeOwnPassword.execute(
				actors.from(jwt),
				id,
				request.currentPassword(),
				request.newPassword());
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Excluir usuário conforme papel e titularidade")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Usuário excluído"),
			@ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Não autenticado\",\"status\":401,\"detail\":\"Autenticação necessária ou token inválido.\"}"))),
			@ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Acesso negado\",\"status\":403,\"detail\":\"Você não tem permissão para executar esta operação.\"}"))),
			@ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Usuário não encontrado\",\"status\":404,\"detail\":\"O usuário solicitado não foi encontrado.\"}"))),
			@ApiResponse(responseCode = "500", description = "Falha interna", content = @Content(
					mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
					examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Erro interno\",\"status\":500,\"detail\":\"Não foi possível concluir a solicitação.\"}")))
	})
	public ResponseEntity<Void> delete(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable @Positive long id
	) {
		deleteUser.execute(actors.from(jwt), id);
		return ResponseEntity.noContent().build();
	}
}
