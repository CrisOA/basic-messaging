package com.exercise.messaging.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping(value = "/", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes =  {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userToCreate) {
        UserValidator.validateUserName(userToCreate);
        UserDTO user = userService.createUser(userToCreate);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
}
