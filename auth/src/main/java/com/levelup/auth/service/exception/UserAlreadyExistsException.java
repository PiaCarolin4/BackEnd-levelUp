package com.levelup.auth.service.exception;

import org.hibernate.service.spi.ServiceException;

public class UserAlreadyExistsException extends ServiceException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}


