package ru.practicum.shareit.exception;

import lombok.Getter;

@Getter
public class ErrorResponse {
    private final int code;
    private final String error;

    public ErrorResponse(int code, String error) {
        this.code = code;
        this.error = error;
    }
}
