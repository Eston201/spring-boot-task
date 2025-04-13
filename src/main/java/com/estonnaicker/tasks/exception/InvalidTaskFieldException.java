package com.estonnaicker.tasks.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidTaskFieldException extends RuntimeException {
    private final String field;

    public InvalidTaskFieldException(String field, String message) {
        super(message);
        this.field = field;
    }
}
