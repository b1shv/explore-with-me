package ru.practicum.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
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
        return errorResponse(e);
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ForbiddenException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(final RuntimeException e) {
        log.warn(e.getMessage(), e);
        return errorResponse(e);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class,
            ValidationException.class,
            MethodArgumentNotValidException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(final RuntimeException e) {
        log.warn(e.getMessage(), e);
        return errorResponse(e);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerError(final Throwable e) {
        log.warn(e.getMessage(), e);
        return errorResponse(e);
    }

    private ErrorResponse errorResponse(Throwable e) {
        if (e.getClass().equals(NotFoundException.class)) {
            return ErrorResponse.builder()
                    .status("NOT_FOUND")
                    .reason("The required object was not found.")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)))
                    .build();
        }
        if (e.getClass().equals(DataIntegrityViolationException.class)) {
            return ErrorResponse.builder()
                    .status("CONFLICT")
                    .reason("Integrity constraint has been violated.")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)))
                    .build();
        }
        if (e.getClass().equals(ForbiddenException.class)) {
            return ErrorResponse.builder()
                    .status("FORBIDDEN")
                    .reason("For the requested operation the conditions are not met.")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)))
                    .build();
        }
        if (e.getClass().equals(IllegalArgumentException.class)
                || e.getClass().equals(MethodArgumentTypeMismatchException.class)
                || e.getClass().equals(ConstraintViolationException.class)
                || e.getClass().equals(ValidationException.class)) {
            return ErrorResponse.builder()
                    .status("BAD_REQUEST")
                    .reason("Incorrectly made request.")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)))
                    .build();
        }
        if (e.getClass().equals(MethodArgumentNotValidException.class)) {
            MethodArgumentNotValidException eCasted = (MethodArgumentNotValidException) e;
            StringBuilder messages = new StringBuilder();
            for (ObjectError error : eCasted.getBindingResult().getAllErrors()) {
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
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)))
                    .build();
        }
        return ErrorResponse.builder()
                .status("INTERNAL_SERVER_ERROR")
                .reason("Something went wrong")
                .message("Server can't process your request right now. Please, try again later")
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)))
                .build();
    }

    @Data
    @Builder
    @AllArgsConstructor
    private static class ErrorResponse {
        private final String status;
        private final String reason;
        private final String message;
        private final String timestamp;
    }
}