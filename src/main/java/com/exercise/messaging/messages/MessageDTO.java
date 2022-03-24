package com.exercise.messaging.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    @JsonProperty("sender_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private long senderId;

    @JsonProperty("receiver_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private long receiverId;

    private String body;

    @JsonProperty("sent_time")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime sentTime;

    public MessageDTO(long receiverId, String body){
        this.receiverId = receiverId;
        this.body = body;
    }
}
