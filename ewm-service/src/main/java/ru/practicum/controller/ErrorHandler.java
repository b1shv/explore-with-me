package ru.practicum.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ObjectUpdateForbiddenException;
import ru.practicum.exception.ValidationException;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(final NotFoundException e) {
        log.warn(e.getMessage(), e);
        return ErrorResponse.builder()
                .status("NOT_FOUND")
                .reason("The required object was not found.")
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler({ObjectUpdateForbiddenException.class, AccessDeniedException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleForbidden(final Exception e) {
        log.warn(e.getMessage(), e);
        return ErrorResponse.builder()
                .status("FORBIDDEN")
                .reason("For the requested operation the conditions are not met.")
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(final DataIntegrityViolationException e) {
        log.warn(e.getMessage(), e);
        return ErrorResponse.builder()
                .status("CONFLICT")
                .reason("Integrity constraint has been violated.")
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(final MethodArgumentNotValidException e) {
        log.warn(e.getMessage(), e);
        StringBuilder messages = new StringBuilder();
        for (ObjectError error : e.getBindingResult().getAllErrors()) {
            messages.append(error.getDefaultMessage());
            messages.append("; ");
        }
        int excessSemicolon = messages.length() - 1;
        int excessWhitespace = messages.length();
        messages.delete(excessSemicolon, excessWhitespace);

        return ErrorResponse.builder()
                .status("BAD_REQUEST")
                .reason("Incorrectly made request.")
                .message(messages.toString())
                .build();
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class,
            ValidationException.class,
            MissingServletRequestParameterException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(final Exception e) {
        log.warn(e.getMessage(), e);
        return ErrorResponse.builder()
                .status("BAD_REQUEST")
                .reason("Incorrectly made request.")
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerError(final Throwable e) {
        log.warn(e.getMessage(), e);
        return ErrorResponse.builder()
                .status("INTERNAL_SERVER_ERROR")
                .reason("Something went wrong")
                .message("The server can't process your request right now. Please, try again later")
                .build();
    }

    @Data
    @Builder
    @AllArgsConstructor
    private static class ErrorResponse {
        private final String status;
        private final String reason;
        private final String message;
        private final String timestamp = (LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
    }
}