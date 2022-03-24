package com.exercise.messaging.messages;

import com.exercise.messaging.exceptions.BadDataException;
import org.apache.commons.lang3.StringUtils;

public class MessageValidator {
    public static void validateMessageSent(MessageDTO message){
        if(message.getReceiverId() < 1){
            throw new BadDataException("Receiver Id must be present and positive");
        }
        if(StringUtils.isBlank(message.getBody())){
            throw new BadDataException("Message body cannot be blank");
        }
    }

    public static void validateSentBy(Long sent_by) {
        if(sent_by != null && sent_by < 1) {
            throw new BadDataException("Request parameter sent_by if provided must be positive");
        }
    }
}
