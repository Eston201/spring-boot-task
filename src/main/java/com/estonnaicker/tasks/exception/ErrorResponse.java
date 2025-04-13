package com.estonnaicker.tasks.exception;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@AllArgsConstructor
@Getter
@Setter
public class ErrorResponse {
    private int status;
    private String message;
    private long timeStamp;
    
    // Does not include errors key in response object if it's empty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> errors;

    public ErrorResponse() {
        this.errors = new HashMap<>();
    }

    public void addError(String key, String value) {
        this.errors.put(key, value);
    }
}
