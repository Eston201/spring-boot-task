package com.estonnaicker.tasks.utils;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private final String status = "success";
    private final T data;

    public ApiResponse(T data) {
        this.data = data;
    }
}