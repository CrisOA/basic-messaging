package com.exercise.messaging.users;

import com.exercise.messaging.exceptions.BadDataException;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class UserValidator {

    public static void validateUserName(UserDTO user){
        if(isBlank(user.getUserName())){
            throw new BadDataException("Field user_name cannot be blank");
        }
    }
}
