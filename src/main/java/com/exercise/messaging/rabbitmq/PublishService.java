package com.exercise.messaging.rabbitmq;

import com.exercise.messaging.messages.MessageDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PublishService {
    @Value("#{systemEnvironment['RABBITMQ_EXCHANGE_NAME']}")
    private String exchangeName = "messages_exchange";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishMessage(MessageDTO message){
        rabbitTemplate.convertAndSend(exchangeName, "", message);
    }
}
