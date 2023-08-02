package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.controller.params.ResultSort;
import ru.practicum.controller.params.SearchParamsPublic;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.service.CategoryService;
import ru.practicum.service.CompilationService;
import ru.practicum.service.EventService;
import ru.practicum.service.StatsService;
import ru.practicum.validation.RangeValidator;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
public class PublicController {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;
    private final EventService eventService;
    private final EventMapper eventMapper;
    private final StatsService statsService;
    private final CompilationService compilationService;
    private final CompilationMapper compilationMapper;
    private final RangeValidator rangeValidator;

    @GetMapping("/events")
    public List<EventShortDto> getAllEvents(@RequestParam(required = false) String text,
                                            @RequestParam(required = false) List<Long> categories,
                                            @RequestParam(required = false) Boolean paid,
                                            @RequestParam(required = false) String rangeStart,
                                            @RequestParam(required = false) String rangeEnd,
                                            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                            @RequestParam(required = false) ResultSort sort,
                                            @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                            @Positive @RequestParam(defaultValue = "10") int size,
                                            HttpServletRequest request) {
        log.debug(String.format("GET /events, params: text=%s, categories=%s, paid=%s, rangeStart=%s," +
                        "rangeEnd=%s, onlyAvailable=%s, sort=%s, from=%d, size=%d", text, categories, paid,
                rangeStart, rangeEnd, onlyAvailable, sort, from, size));
        SearchParamsPublic params = SearchParamsPublic.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .build();
        List<Event> events = eventService.getAllEvents(rangeValidator.parseAndValidate(params, rangeStart, rangeEnd));
        statsService.hit(request.getRequestURI(), request.getRemoteAddr());
        List<EventShortDto> eventsDto = eventMapper.toShortDto(events, statsService.getEventsViews(events));

        if (sort != null && sort.equals(ResultSort.VIEWS)) {
            eventsDto.sort(Comparator.comparing(EventShortDto::getViews));
        }
        return eventsDto;
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto getEventById(@PathVariable long eventId, HttpServletRequest request) {
        log.debug(String.format("GET /events/%d", eventId));
        Event event = eventService.getPublishedEventById(eventId);
        statsService.hit(request.getRequestURI(), request.getRemoteAddr());
        return eventMapper.toFullDto(event, statsService.getEventViews(event));
    }

    @GetMapping("/categories")
    public List<CategoryDto> getAllCategories(@PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                              @Positive @RequestParam(defaultValue = "10") int size) {
        log.debug(String.format("GET /categories, params: from=%d, size=%d", from, size));
        int page = from / size;
        return categoryService.getAllCategories(PageRequest.of(page, size)).stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/categories/{categoryId}")
    public CategoryDto getCategoryById(@PathVariable long categoryId) {
        log.debug(String.format("GET /categories/%d", categoryId));
        return categoryMapper.toDto(categoryService.getCategoryById(categoryId));
    }

    @GetMapping("/compilations")
    public List<CompilationDto> getAllCompilations(@RequestParam(required = false) Boolean pinned,
                                                   @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                   @Positive @RequestParam(defaultValue = "10") int size) {
        log.debug(String.format("GET /compilations, params: pinned=%s, from=%d, size=%d", pinned, from, size));
        int page = from / size;
        List<Compilation> compilations = compilationService.getAllCompilations(pinned, PageRequest.of(page, size));
        List<Event> events = new ArrayList<>();
        compilations.forEach(compilation -> events.addAll(compilation.getEvents()));
        return compilationMapper.toDto(compilations, statsService.getEventsViews(events));
    }

    @GetMapping("/compilations/{compilationId}")
    public CompilationDto getCompilationById(@PathVariable long compilationId) {
        log.debug(String.format("GET /compilations/%d", compilationId));
        Compilation compilation = compilationService.getCompilationById(compilationId);
        List<Event> events = new ArrayList<>(compilation.getEvents());
        return compilationMapper.toDto(compilation, events, statsService.getEventsViews(events));
    }
}
