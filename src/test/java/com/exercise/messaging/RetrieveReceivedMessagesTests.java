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
class RetrieveReceivedMessagesTests {

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


	public void setSuccessfulScenario(User receiver, User sender1, User sender2){
		messageRepository.save(new Message(sender1, receiver, "message 1", LocalDateTime.now()));
		messageRepository.save(new Message(sender1, receiver, "message 2", LocalDateTime.now()));
		messageRepository.save(new Message(sender2, receiver, "message 3", LocalDateTime.now()));
	}

	@AfterEach
	public void tearDown(){
		userRepository.deleteAll();
		messageRepository.deleteAll();
	}

	@Test
	void successfulTest() throws Exception {
		User user1 = new User ("User_1");
		User receiver = userRepository.save(user1);
		User user2 = new User ("User_2");
		User sender1 = userRepository.save(user2);
		User user3 = new User ("User_3");
		User sender2 = userRepository.save(user3);
		setSuccessfulScenario(receiver, sender1, sender2);

		MvcResult result = mockMvc.perform(get("/api/v1/messages/received")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.header("x-user-id", receiver.getId()))
				.andExpect(status().isOk()).andReturn();

		String stringContent = result.getResponse().getContentAsString();

		List<Map<String, Object>> messages = objectMapper.readValue(stringContent, List.class);

		assertThat(messages).isNotEmpty();
		assertThat(messages.size()).isEqualTo(3);
		assertThat(messages.get(0).get("body")).isEqualTo("message 1");
		assertThat(messages.get(1).get("body")).isEqualTo("message 2");
		assertThat(messages.get(2).get("body")).isEqualTo("message 3");
		assertThat(messages.get(0).get("sender_id")).isEqualTo(((int)sender1.getId()));
		assertThat(messages.get(1).get("sender_id")).isEqualTo(((int)sender1.getId()));
		assertThat(messages.get(2).get("sender_id")).isEqualTo(((int)sender2.getId()));
	}

	@Test
	void receiverNotFoundTest() throws Exception {
		long nonExsitingId = 10;

		 mockMvc.perform(get("/api/v1/messages/sent")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.header("x-user-id", nonExsitingId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error_message").value("Not found Sender User with id = " + nonExsitingId));
	}

	@Test
	void badRequestLoggedUserNotPresentTest() throws Exception {
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

	@Test
	void successfulFilteredBySenderTest() throws Exception {
		User user1 = new User ("User_1");
		User receiver = userRepository.save(user1);
		User user2 = new User ("User_2");
		User sender1 = userRepository.save(user2);
		User user3 = new User ("User_3");
		User sender2 = userRepository.save(user3);
		
		setSuccessfulScenario(receiver, sender1, sender2);

		MvcResult resultSender1 = mockMvc.perform(get("/api/v1/messages/received")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.header("x-user-id", receiver.getId())
						.param("sent_by", String.valueOf(sender1.getId())))
				.andExpect(status().isOk()).andReturn();

		String stringContentSender1 = resultSender1.getResponse().getContentAsString();

		List<Map<String, Object>> messagesSender1 = objectMapper.readValue(stringContentSender1, List.class);

		assertThat(messagesSender1).isNotEmpty();
		assertThat(messagesSender1.size()).isEqualTo(2);
		assertThat(messagesSender1.get(0).get("body")).isEqualTo("message 1");
		assertThat(messagesSender1.get(1).get("body")).isEqualTo("message 2");
		assertThat(messagesSender1.get(0).get("sender_id")).isEqualTo(((int)sender1.getId()));
		assertThat(messagesSender1.get(1).get("sender_id")).isEqualTo(((int)sender1.getId()));

		MvcResult resultSender2 = mockMvc.perform(get("/api/v1/messages/received")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.header("x-user-id", receiver.getId())
						.param("sent_by", String.valueOf(sender2.getId())))
				.andExpect(status().isOk()).andReturn();

		String stringContentSender2 = resultSender2.getResponse().getContentAsString();

		List<Map<String, Object>> messagesSender2 = objectMapper.readValue(stringContentSender2, List.class);

		assertThat(messagesSender2).isNotEmpty();
		assertThat(messagesSender2.size()).isEqualTo(1);
		assertThat(messagesSender2.get(0).get("body")).isEqualTo("message 3");
		assertThat(messagesSender2.get(0).get("sender_id")).isEqualTo(((int)sender2.getId()));
	}

	@Test
	void notFoundSenderIdFilteredBySenderTest() throws Exception {
		User user1 = new User ("User_1");
		User receiver = userRepository.save(user1);
		String nonExistingId = String.valueOf(receiver.getId() + 5);
		mockMvc.perform(get("/api/v1/messages/received")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.header("x-user-id", receiver.getId())
						.param("sent_by", nonExistingId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error_message").value("Not found Sender User with id = " + nonExistingId ));
	}

	@Test
	void badRequestSenderIdOutOfRangeFilteredBySenderTest() throws Exception {
		User user1 = new User ("User_1");
		User receiver = userRepository.save(user1);
		String nonExistingId = "-1";
		mockMvc.perform(get("/api/v1/messages/received")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.header("x-user-id", receiver.getId())
						.param("sent_by", nonExistingId))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error_message").value("Request parameter sent_by if provided must be positive"));
	}

}
