package com.exercise.messaging.messages;

import java.util.List;

import com.exercise.messaging.users.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySender(User sender);
    List<Message> findByReceiver(User receiver);
    List<Message> findBySenderAndReceiverOrderBySentTimeAsc(User sender, User receiver);
}