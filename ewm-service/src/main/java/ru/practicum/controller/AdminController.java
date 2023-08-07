package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.controller.params.SearchParamsAdmin;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.CategoryRequest;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationRequest;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.user.UserFullDto;
import ru.practicum.dto.user.UserRequest;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.model.state.event.EventState;
import ru.practicum.service.CategoryService;
import ru.practicum.service.CommentService;
import ru.practicum.service.CompilationService;
import ru.practicum.service.EventService;
import ru.practicum.service.StatsService;
import ru.practicum.service.UserService;
import ru.practicum.validation.Create;
import ru.practicum.validation.RangeValidator;
import ru.practicum.validation.Update;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/admin")
@Validated
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;
    private final EventService eventService;
    private final EventMapper eventMapper;
    private final StatsService statsService;
    private final CompilationService compilationService;
    private final CompilationMapper compilationMapper;
    private final RangeValidator rangeValidator;
    private final CommentService commentService;

    @GetMapping("/events")
    public List<EventFullDto> getAllEvents(@RequestParam(required = false) List<Long> users,
                                           @RequestParam(required = false) List<EventState> states,
                                           @RequestParam(required = false) List<Long> categories,
                                           @RequestParam(required = false) String rangeStart,
                                           @RequestParam(required = false) String rangeEnd,
                                           @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                           @Positive @RequestParam(defaultValue = "10") int size) {
        log.debug(String.format("GET /admin/events, params: users=%s, states=%s, categories=%s, rangeStart=%s," +
                "rangeEnd=%s, from=%d, size=%d", users, states, categories, rangeStart, rangeEnd, from, size));
        int page = from / size;
        SearchParamsAdmin params = SearchParamsAdmin.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .pageable(PageRequest.of(page, size))
                .build();
        List<Event> events = eventService.getAllEvents(rangeValidator.parseAndValidate(params, rangeStart, rangeEnd));
        return eventMapper.toFullDto(events, statsService.getEventsViews(events));
    }

    @PatchMapping("/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable long eventId,
                                    @Validated(Update.class) @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        log.debug(String.format("PATCH /admin/events/%d, body=%s", eventId, updateEventAdminRequest));
        Event event = eventService.updateEvent(eventId, updateEventAdminRequest);
        return eventMapper.toFullDto(event, statsService.getEventViews(event));
    }

    @GetMapping("/users")
    public List<UserFullDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                      @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                      @Positive @RequestParam(defaultValue = "10") int size) {
        log.debug(String.format("GET /admin/users, params: ids=%s, from=%d, size=%d", ids, from, size));
        int page = from / size;
        return userService.getUsers(ids, PageRequest.of(page, size)).stream()
                .map(userMapper::toFullDto)
                .collect(Collectors.toList());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users")
    public UserFullDto addUser(@Valid @RequestBody UserRequest userRequest) {
        log.debug(String.format("POST /admin/users, body=%s", userRequest));
        return userMapper.toFullDto(userService.addUser(userMapper.toUser(userRequest)));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/users/{userId}")
    public void deleteUser(@PathVariable long userId) {
        log.debug(String.format("DELETE /admin/users/%d,", userId));
        userService.deleteUser(userId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/categories")
    public CategoryDto addCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        log.debug(String.format("POST /admin/categories, body=%s", categoryRequest));
        return categoryMapper.toDto(categoryService.addCategory(categoryMapper.toCategory(categoryRequest)));
    }

    @PatchMapping("/categories/{categoryId}")
    public CategoryDto updateCategory(@PathVariable long categoryId,
                                      @Valid @RequestBody CategoryRequest categoryRequest) {
        log.debug(String.format("PATCH /admin/categories/%d, body=%s", categoryId, categoryRequest));
        return categoryMapper.toDto(categoryService.updateCategory(categoryMapper.toCategory(categoryRequest, categoryId)));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/categories/{categoryId}")
    public void deleteCategory(@PathVariable long categoryId) {
        log.debug(String.format("DELETE /admin/categories/%d", categoryId));
        categoryService.deleteCategory(categoryId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/compilations")
    public CompilationDto addCompilation(@Validated(Create.class) @RequestBody CompilationRequest compilationRequest) {
        log.debug(String.format("POST /admin/compilations, body=%s", compilationRequest));
        List<Event> events = eventService.getEventsByIds(compilationRequest.getEvents());
        Compilation compilationReturned = compilationService.addCompilation(
                compilationMapper.toCompilation(compilationRequest, new HashSet<>(events)));
        return compilationMapper.toDto(compilationReturned, events, statsService.getEventsViews(events));
    }

    @PatchMapping("/compilations/{compilationId}")
    public CompilationDto updateCompilation(@PathVariable long compilationId,
                                            @Validated(Update.class) @RequestBody CompilationRequest compilationRequest) {
        log.debug(String.format("PATCH /admin/compilations/%d, body=%s", compilationId, compilationRequest));
        Compilation compilationReturned = compilationService.updateCompilation(compilationId, compilationRequest);
        List<Event> events = new ArrayList<>(compilationReturned.getEvents());
        return compilationMapper.toDto(compilationReturned, events, statsService.getEventsViews(events));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/compilations/{compilationId}")
    public void deleteCompilation(@PathVariable long compilationId) {
        log.debug(String.format("DELETE /admin/compilations/%d", compilationId));
        compilationService.deleteCompilation(compilationId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/comments/{commentId}")
    public void deleteComment(@PathVariable long commentId) {
        log.debug(String.format("DELETE /admin/comments/%d", commentId));
        commentService.deleteComment(commentId);
    }
}
