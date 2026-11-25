package br.com.fiap.techchallenge.usermanagement.adapter.in.web;

import java.net.URI;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.com.fiap.techchallenge.usermanagement.application.exception.DuplicateUserAttribute;
import br.com.fiap.techchallenge.usermanagement.application.exception.InvalidCredentialsException;
import br.com.fiap.techchallenge.usermanagement.application.exception.InvalidCurrentPasswordException;
import br.com.fiap.techchallenge.usermanagement.application.exception.UserConflictException;
import br.com.fiap.techchallenge.usermanagement.application.exception.UserForbiddenException;
import br.com.fiap.techchallenge.usermanagement.application.exception.UserNotFoundException;
import br.com.fiap.techchallenge.usermanagement.domain.DomainValidationException;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(DomainValidationException.class)
	ProblemDetail domainValidation() {
		return problem(HttpStatus.BAD_REQUEST, "Requisição inválida",
				"Um ou mais dados informados são inválidos.");
	}

	@ExceptionHandler(InvalidCurrentPasswordException.class)
	ProblemDetail invalidCurrentPassword() {
		return problem(HttpStatus.BAD_REQUEST, "Requisição inválida", "A senha atual não confere.");
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	ProblemDetail invalidCredentials() {
		return problem(HttpStatus.UNAUTHORIZED, "Não autenticado", "Login ou senha inválidos.");
	}

	@ExceptionHandler(UserForbiddenException.class)
	ProblemDetail forbidden() {
		return problem(HttpStatus.FORBIDDEN, "Acesso negado", "Você não tem permissão para executar esta operação.");
	}

	@ExceptionHandler(UserNotFoundException.class)
	ProblemDetail notFound() {
		return problem(HttpStatus.NOT_FOUND, "Usuário não encontrado", "O usuário solicitado não foi encontrado.");
	}

	@ExceptionHandler(UserConflictException.class)
	ProblemDetail conflict(UserConflictException exception) {
		String detail = exception.attribute() == DuplicateUserAttribute.EMAIL
				? "O e-mail informado já está em uso."
				: "O login informado já está em uso.";
		ProblemDetail problem = problem(HttpStatus.CONFLICT, "Conflito de usuário", detail);
		problem.setProperty("attribute", exception.attribute().name().toLowerCase(Locale.ROOT));
		return problem;
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException exception,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request
	) {
		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Requisição inválida",
				"Um ou mais campos não atendem aos critérios de validação.");
		List<ValidationError> errors = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
				.toList();
		problem.setProperty("errors", errors);
		return handleExceptionInternal(exception, problem, headers, HttpStatus.BAD_REQUEST, request);
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(
			MissingServletRequestParameterException exception,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request
	) {
		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Requisição inválida",
				"Um parâmetro obrigatório está ausente.");
		return handleExceptionInternal(exception, problem, headers, HttpStatus.BAD_REQUEST, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(
			HttpMessageNotReadableException exception,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request
	) {
		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Requisição inválida",
				"O corpo da requisição está ausente ou malformado, ou contém campos não suportados.");
		return handleExceptionInternal(exception, problem, headers, HttpStatus.BAD_REQUEST, request);
	}

	@Override
	protected ResponseEntity<Object> handleHandlerMethodValidationException(
			HandlerMethodValidationException exception,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request
	) {
		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Requisição inválida",
				"Um ou mais parâmetros não atendem aos critérios de validação.");
		return handleExceptionInternal(exception, problem, headers, HttpStatus.BAD_REQUEST, request);
	}

	@Override
	protected ResponseEntity<Object> handleTypeMismatch(
			TypeMismatchException exception,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request
	) {
		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Requisição inválida",
				"Um parâmetro possui formato inválido.");
		return handleExceptionInternal(exception, problem, headers, HttpStatus.BAD_REQUEST, request);
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(
			Exception exception,
			Object body,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request
	) {
		if (body instanceof ProblemDetail problem && status.is5xxServerError()) {
			problem.setTitle("Erro interno");
			problem.setDetail("Não foi possível concluir a solicitação.");
		}
		return super.handleExceptionInternal(exception, body, headers, status, request);
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ProblemDetail> unexpected() {
		LOGGER.error("Falha inesperada ao processar uma requisição da API");
		return ResponseEntity.internalServerError().body(
				problem(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
						"Não foi possível concluir a solicitação."));
	}

	private static ProblemDetail problem(HttpStatus status, String title, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setType(URI.create("about:blank"));
		problem.setTitle(title);
		return problem;
	}

	private record ValidationError(String field, String message) {
	}
}
