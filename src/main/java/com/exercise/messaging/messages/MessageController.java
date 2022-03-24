package com.exercise.messaging.messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@Validated
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping(value = "/", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes =  {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<HttpStatus> sendMessage(
            @RequestHeader("x-user-id") @Min(1) long loggedUser,
            @RequestBody MessageDTO messageRequest) {
        MessageValidator.validateMessageSent(messageRequest);
        this.messageService.sendMessage(loggedUser, messageRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(value = "/sent", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<MessageDTO>> sentMessages(
            @RequestHeader("x-user-id") @Min(1) long loggedUser
    ) {
        List<MessageDTO> messages = this.messageService.getMessagesBySender(loggedUser);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @GetMapping(value = "/received", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<MessageDTO>> receivedMessages(
            @RequestHeader("x-user-id") @Min(1) long loggedUser,
        @RequestParam(required = false) Long sent_by) {
        List<MessageDTO> messages;
        if(sent_by == null) {
                messages = this.messageService.getMessagesByReceiver(loggedUser);
        }
        else{
            MessageValidator.validateSentBy(sent_by);
            messages = this.messageService.getMessagesBySenderAndReceiver(sent_by, loggedUser);
        }
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }
}
