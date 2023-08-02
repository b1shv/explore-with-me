package ru.practicum.validation;

import org.springframework.stereotype.Component;
import ru.practicum.controller.params.SearchParamsAdmin;
import ru.practicum.controller.params.SearchParamsPublic;
import ru.practicum.exception.ValidationException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class RangeValidator {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public SearchParamsAdmin parseAndValidate(SearchParamsAdmin params, String rangeStart, String rangeEnd) {
        LocalDateTime rangeStartParsed = parse(rangeStart);
        LocalDateTime rangeEndParsed = parse(rangeEnd);

        validate(rangeStartParsed, rangeEndParsed);

        params.setRangeStart(rangeStartParsed);
        params.setRangeEnd(rangeEndParsed);
        return params;
    }

    public SearchParamsPublic parseAndValidate(SearchParamsPublic params, String rangeStart, String rangeEnd) {
        LocalDateTime rangeStartParsed = parse(rangeStart);
        LocalDateTime rangeEndParsed = parse(rangeEnd);

        validate(rangeStartParsed, rangeEndParsed);

        params.setRangeStart(rangeStartParsed);
        params.setRangeEnd(rangeEndParsed);
        return params;
    }

    private LocalDateTime parse(String dateTime) {
        if (dateTime == null) {
            return null;
        }
        return LocalDateTime.parse(
                URLDecoder.decode(dateTime, StandardCharsets.UTF_8),
                DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
    }

    private void validate(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Range start must be before range end");
        }
    }
}
