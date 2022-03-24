package com.exercise.messaging;

import com.exercise.messaging.messages.Message;
import com.exercise.messaging.messages.MessageRepository;
import com.exercise.messaging.users.User;
import com.exercise.messaging.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
class SendMessagesTests {

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

	private List<User> users;

	@BeforeEach
	public void setUp(){
		User user1 = new User ("User_1");
		User user2 = new User ("User_2");
		User storedUser1 = userRepository.save(user1);
		User storedUser2 = userRepository.save(user2);
		users = Lists.list(user1, user2);
	}

	@AfterEach
	public void tearDown(){
		userRepository.deleteAll();
		users.clear();
	}

	@Test
	void successfulTest() throws Exception {
		Map<String, Object> payload = new HashMap<>();
		User receiver =  users.get(0);
		User sender =  users.get(1);

		payload.put("receiver_id", receiver.getId());
		payload.put("body", "test message");
		String jsonPayload = objectMapper.writeValueAsString(payload);

		MvcResult result = mockMvc.perform(post("/api/v1/messages/")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.header("x-user-id", sender.getId())
						.content(jsonPayload))
				.andExpect(status().isCreated()).andReturn();

		String stringContent = result.getResponse().getContentAsString();

		List<Message> messages = messageRepository.findBySenderAndReceiverOrderBySentTimeAsc(sender, receiver);

		assertThat(messages).isNotEmpty();
		assertThat(messages.size()).isEqualTo(1);
		assertThat(messages.get(0).getBody()).isEqualTo("test message");
	}

	@Test
	void senderNotFoundTest() throws Exception {
		Map<String, Object> payload = new HashMap<>();
		User receiver =  users.get(0);
		User sender =  users.get(1);
		long nonExsitingId = receiver.getId() + sender.getId();

		payload.put("receiver_id", receiver.getId());
		payload.put("body", "test message");
		String jsonPayload = objectMapper.writeValueAsString(payload);

		 mockMvc.perform(post("/api/v1/messages/")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.header("x-user-id", nonExsitingId)
						.content(jsonPayload))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error_message").value("Not found Sender User with id = " + nonExsitingId));
	}

	@Test
	void badRequestSenderNotPresentTest() throws Exception {
		Map<String, Object> payload = new HashMap<>();
		User receiver =  users.get(0);

		payload.put("receiver_id", receiver.getId());
		payload.put("body", "test message");
		String jsonPayload = objectMapper.writeValueAsString(payload);

		mockMvc.perform(post("/api/v1/messages/")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonPayload))
				.andExpect(status().isBadRequest());
	}

	@Test
	void notFoundLoggedUserIdOutOfRangeTest() throws Exception {
		Map<String, Object> payload = new HashMap<>();
		User receiver =  users.get(0);

		payload.put("receiver_id", receiver.getId());
		payload.put("body", "test message");
		String jsonPayload = objectMapper.writeValueAsString(payload);

		mockMvc.perform(post("/api/v1/messages/")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.header("x-user-id", -1)
						.content(jsonPayload))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error_message").value("sendMessage.loggedUser: must be greater than or equal to 1" ));
	}

	@Test
	void receiverNotFoundTest() throws Exception {
		Map<String, Object> payload = new HashMap<>();
		User receiver =  users.get(0);
		User sender =  users.get(1);
		long nonExsitingId = receiver.getId() + sender.getId();

		payload.put("receiver_id", nonExsitingId);
		payload.put("body", "test message");
		String jsonPayload = objectMapper.writeValueAsString(payload);

		mockMvc.perform(post("/api/v1/messages/")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.header("x-user-id", sender.getId())
						.content(jsonPayload))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error_message").value("Not found Receiver User with id = " + nonExsitingId));
	}

	@Test
	void badRequestReceiverNotPresentTest() throws Exception {
		Map<String, Object> payload = new HashMap<>();
		User sender =  users.get(1);

		payload.put("body", "test message");
		String jsonPayload = objectMapper.writeValueAsString(payload);

		mockMvc.perform(post("/api/v1/messages/")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.header("x-user-id", sender.getId())
						.content(jsonPayload))
				.andExpect(status().isBadRequest());
	}

	@Test
	void badRequestBlankBodyTest() throws Exception {
		Map<String, Object> payload = new HashMap<>();
		User receiver =  users.get(0);
		User sender =  users.get(1);
		long nonExsitingId = receiver.getId() + sender.getId();

		payload.put("receiver_id", receiver.getId());
		String jsonPayload = objectMapper.writeValueAsString(payload);

		mockMvc.perform(post("/api/v1/messages/")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.header("x-user-id", nonExsitingId)
						.content(jsonPayload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error_message").value("Message body cannot be blank"));
	}

}
