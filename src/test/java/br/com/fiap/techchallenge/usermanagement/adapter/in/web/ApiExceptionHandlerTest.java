package br.com.fiap.techchallenge.usermanagement.adapter.in.web;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class ApiExceptionHandlerTest {

	@Test
	void unexpectedFailuresReturnGenericProblemDetailWithoutInternalMessage() throws Exception {
		String internalMessage = "SQL password_hash=private-value";
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new FailureController(internalMessage))
				.setControllerAdvice(new ApiExceptionHandler())
				.build();

		var response = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.get("/internal-failure"))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status()
						.isInternalServerError())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
						.contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status")
						.value(500))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.detail")
						.value("Não foi possível concluir a solicitação."))
				.andReturn();

		assertFalse(response.getResponse().getContentAsString().contains(internalMessage));
	}

	@RestController
	static class FailureController {

		private final String internalMessage;

		FailureController(String internalMessage) {
			this.internalMessage = internalMessage;
		}

		@GetMapping("/internal-failure")
		String fail() {
			throw new IllegalStateException(internalMessage);
		}
	}
}
