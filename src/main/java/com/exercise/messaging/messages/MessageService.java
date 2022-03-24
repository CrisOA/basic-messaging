package com.exercise.messaging.messages;

import com.exercise.messaging.exceptions.ResourceNotFoundException;
import com.exercise.messaging.rabbitmq.PublishService;
import com.exercise.messaging.users.User;
import com.exercise.messaging.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PublishService publisher;

    void sendMessage(long senderId, MessageDTO message) throws ResourceNotFoundException{
        User sender = getSender(senderId);
        User receiver = getReceiver(message.getReceiverId());
        LocalDateTime sent_time = LocalDateTime.now();
        Message messageToStore = new Message(sender, receiver, message.getBody(), sent_time);
        Message storedMessage = this.messageRepository.save(messageToStore);
        MessageDTO messageToPublish = this.mapMessage(storedMessage);
        publisher.publishMessage(messageToPublish);
    }

    private User getReceiver(long receiverId)  throws ResourceNotFoundException{
        User receiver = userRepository.findById(receiverId).orElseThrow(
                () -> new ResourceNotFoundException("Not found Receiver User with id = " + receiverId)
        );
        return receiver;
    }

    private User getSender(long senderId)  throws ResourceNotFoundException{
        User sender = this.userRepository.findById(senderId).orElseThrow(
                () -> new ResourceNotFoundException("Not found Sender User with id = " + senderId)
        );
        return sender;
    }

    public List<MessageDTO> getMessagesBySender(long senderId){
        User sender = getSender(senderId);
        List<MessageDTO> messagesToReturn = this.messageRepository.findBySender(sender)
                .stream()
                .map(this::mapMessage).collect(Collectors.toList());
        return messagesToReturn;
    }

    private MessageDTO mapMessage(Message message){
        User sender = message.getSender();
        User receiver = message.getReceiver();
        MessageDTO messageToReturn = new MessageDTO(sender.getId(), receiver.getId(), message.getBody(), message.getSentTime());
        return messageToReturn;
    }

    List<MessageDTO> getMessagesByReceiver(long receiverId){
        User receiver = getReceiver(receiverId);
        List<MessageDTO> messagesToReturn = this.messageRepository.findByReceiver(receiver)
                .stream()
                .map(this::mapMessage).collect(Collectors.toList());
        return messagesToReturn;
    }

    List<MessageDTO> getMessagesBySenderAndReceiver(long senderId, long receiverId){
        User sender = getSender(senderId);
        User receiver = getReceiver(receiverId);
        List<MessageDTO> messagesToReturn = this.messageRepository.findBySenderAndReceiverOrderBySentTimeAsc(sender, receiver)
                .stream()
                .map(this::mapMessage).collect(Collectors.toList());
        return messagesToReturn;
    }
}
