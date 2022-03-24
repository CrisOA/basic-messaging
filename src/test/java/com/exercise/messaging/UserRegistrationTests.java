package com.exercise.messaging;

import com.exercise.messaging.users.User;
import com.exercise.messaging.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
class UserRegistrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@AfterEach
	public void tearDown(){
		userRepository.deleteAll();
	}

	@MockBean
	private RabbitTemplate rabbitTemplate;

	@Test
	void successfulTest() throws Exception {

		MvcResult result = mockMvc.perform(post("/api/v1/users/")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"user_name\": \"my_name\"}"))
				.andExpect(status().isCreated()).andReturn();

		String stringContent = result.getResponse().getContentAsString();

		Map<String, Integer> responseBody = objectMapper.readValue(stringContent, HashMap.class);
		Long userId = new Long(responseBody.get("user_id"));
		Optional<User> user = userRepository.findById(userId);
		assertThat(user.isPresent()).isTrue();
		assertThat(user.get().getUserName()).isEqualTo("my_name");
	}

	@Test
	void badRequestTest() throws Exception {

		mockMvc.perform(post("/api/v1/users/")
						.contentType("application/json")
						.content("{\"user_name\": \"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error_message").value("Field user_name cannot be blank"));

	}

	@Test
	void nameConflictTest() throws Exception {

		mockMvc.perform(post("/api/v1/users/")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"user_name\": \"my_name\"}"))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/users/")
						.contentType("application/json")
						.content("{\"user_name\": \"my_name\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error_message").value("User name already in use, try different one."));
	}

}
