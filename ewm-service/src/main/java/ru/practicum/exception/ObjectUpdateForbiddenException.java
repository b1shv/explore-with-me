package ru.practicum.exception;

public class ObjectUpdateForbiddenException extends RuntimeException {
    public ObjectUpdateForbiddenException() {
    }

    public ObjectUpdateForbiddenException(String message) {
        super(message);
    }
}
