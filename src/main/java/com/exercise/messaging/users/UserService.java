package com.exercise.messaging.users;

import com.exercise.messaging.exceptions.ResourceExistsConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public UserDTO createUser(UserDTO userToCreate){
        User user = new User(userToCreate.getUserName());
        User createdUser = saveUser(user);
        UserDTO userToReturn = new UserDTO();
        userToReturn.setUserId(createdUser.getId());
        return userToReturn;
    }

    private User saveUser(User user) {
        try{
            User createdUser = userRepository.save(user);
            return createdUser;
        }
        catch(DataIntegrityViolationException exception){
            throw new ResourceExistsConflictException("User name already in use, try different one.");
        }
    }
}
