package com.exercise.messaging;

import com.exercise.messaging.messages.Message;
import com.exercise.messaging.messages.MessageRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
class RetrieveSentMessagesTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MessageRepository messageRepository;

	@MockBean
	private RabbitTemplate rabbitTemplate;


	public void setSuccessfulScenario(User sender){
		User user2 = new User ("User_2");
		User receiver = userRepository.save(user2);
		messageRepository.save(new Message(sender, receiver, "message 1", LocalDateTime.now()));
		messageRepository.save(new Message(sender, receiver, "message 2", LocalDateTime.now()));
	}

	@AfterEach
	public void tearDown(){
		userRepository.deleteAll();
		messageRepository.deleteAll();
	}

	@Test
	void successfulTest() throws Exception {
		User user1 = new User ("User_1");
		User sender = userRepository.save(user1);
		setSuccessfulScenario(sender);

		MvcResult result = mockMvc.perform(get("/api/v1/messages/sent")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.header("x-user-id", sender.getId()))
				.andExpect(status().isOk()).andReturn();

		String stringContent = result.getResponse().getContentAsString();

		List<Map<String, Object>> messages = objectMapper.readValue(stringContent, List.class);

		assertThat(messages).isNotEmpty();
		assertThat(messages.size()).isEqualTo(2);
		assertThat(messages.get(0).get("body")).isEqualTo("message 1");
		assertThat(messages.get(1).get("body")).isEqualTo("message 2");
	}

	@Test
	void senderNotFoundTest() throws Exception {
		long nonExsitingId = 10;

		 mockMvc.perform(get("/api/v1/messages/sent")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.header("x-user-id", nonExsitingId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error_message").value("Not found Sender User with id = " + nonExsitingId));
	}

	@Test
	void badRequestSenderNotPresentTest() throws Exception {
		mockMvc.perform(get("/api/v1/messages/sent")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	void notFoundLoggedUserIdOutOfRangeTest() throws Exception {
		mockMvc.perform(get("/api/v1/messages/sent")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.header("x-user-id", -1))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error_message").value("sentMessages.loggedUser: must be greater than or equal to 1" ));
	}

}
