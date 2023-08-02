package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.HitDto;
import ru.practicum.StatsDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.service.StatsService;

import javax.validation.Valid;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class StatsController {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void addHit(@RequestBody @Valid HitDto hitDto) {
        log.debug("POST /hit, body: {}", hitDto);
        statsService.addHit(hitDto);
    }

    @GetMapping("/stats")
    public List<StatsDto> getStats(@RequestParam String start,
                                   @RequestParam String end,
                                   @RequestParam(required = false) String[] uris,
                                   @RequestParam(defaultValue = "false") boolean unique) {
        log.debug("GET /stats, params: start={} end={} uris={} unique={}", start, end, uris, unique);
        LocalDateTime startParsed = parseDateTimeParam(start);
        LocalDateTime endParsed = parseDateTimeParam(end);
        if (startParsed.isAfter(endParsed)) {
            throw new ValidationException("Start must be before end");
        }
        return statsService.getStats(parseDateTimeParam(start), parseDateTimeParam(end), unique, uris);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public Map<String, String> handleBadRequest(final ValidationException e) {
        return Map.of(
                "status", "BAD_REQUEST",
                "reason", "Incorrectly made request.",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        );
    }

    private LocalDateTime parseDateTimeParam(String param) {
        return LocalDateTime.parse(
                URLDecoder.decode(param, StandardCharsets.UTF_8),
                DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
    }
}
