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
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentModerationRequest;
import ru.practicum.dto.comment.CommentModerationResult;
import ru.practicum.dto.comment.CommentRequest;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestModerationRequest;
import ru.practicum.dto.request.RequestModerationResult;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.model.state.CommentState;
import ru.practicum.service.CategoryService;
import ru.practicum.service.CommentService;
import ru.practicum.service.EventService;
import ru.practicum.service.RequestService;
import ru.practicum.service.StatsService;
import ru.practicum.service.UserService;
import ru.practicum.validation.Create;
import ru.practicum.validation.Update;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PrivateController {
    private final EventService eventService;
    private final EventMapper eventMapper;
    private final CategoryService categoryService;
    private final UserService userService;
    private final StatsService statsService;
    private final RequestService requestService;
    private final RequestMapper requestMapper;
    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @GetMapping("/{userId}/events")
    public List<EventShortDto> getEventsByUserId(@PathVariable long userId,
                                                 @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                 @Positive @RequestParam(defaultValue = "10") int size) {
        log.debug(String.format("GET /users/%d/events, params: from=%d, size=%d", userId, from, size));
        int page = from / size;
        List<Event> events = eventService.getEventsByUserId(userId, PageRequest.of(page, size));
        return eventMapper.toShortDto(events, statsService.getEventsViews(events));
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getEvent(@PathVariable long userId,
                                 @PathVariable long eventId) {
        log.debug(String.format("GET /users/%d/events/%d", userId, eventId));
        Event event = eventService.getUserEventById(eventId, userId);
        return eventMapper.toFullDto(event, statsService.getEventViews(event));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{userId}/events")
    public EventFullDto addEvent(@PathVariable long userId,
                                 @RequestBody @Validated(Create.class) EventDto eventDto) {
        log.debug(String.format("POST /users/%d/events, body=%s", userId, eventDto));
        Category category = categoryService.getCategoryById(eventDto.getCategory());
        User initiator = userService.getUserById(userId);
        return eventMapper.toFullDto(eventService.addEvent(eventMapper.toEvent(eventDto, category, initiator)));
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable long userId,
                                    @PathVariable long eventId,
                                    @RequestBody @Validated(Update.class) UpdateEventUserRequest updateEventUserRequest) {
        log.debug(String.format("PATCH /users/%d/events/%d, body=%s", userId, eventId, updateEventUserRequest));
        Event event = eventService.updateEvent(eventId, userId, updateEventUserRequest);
        return eventMapper.toFullDto(event, statsService.getEventViews(event));
    }

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getRequestsByUserId(@PathVariable long userId) {
        log.debug(String.format("GET /users/%d/requests", userId));
        return requestMapper.toDto(requestService.getRequestsByUserId(userId));
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsByEventId(@PathVariable long userId,
                                                              @PathVariable long eventId) {
        log.debug(String.format("GET /users/%d/events/%d/requests", userId, eventId));
        return requestMapper.toDto(requestService.getRequestsByEventId(userId, eventId));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{userId}/requests")
    public ParticipationRequestDto addRequest(@PathVariable long userId,
                                              @RequestParam long eventId) {
        log.debug(String.format("POST /users/%d/requests, params: eventId=%d", userId, eventId));
        return requestMapper.toDto(requestService.addRequest(requestMapper.createRequest(eventId, userId)));
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable long userId,
                                                 @PathVariable long requestId) {
        log.debug(String.format("PATCH /users/%d/requests/%d/cancel", userId, requestId));
        return requestMapper.toDto(requestService.cancelRequest(userId, requestId));
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public RequestModerationResult updateRequestStatus(@PathVariable long userId,
                                                       @PathVariable long eventId,
                                                       @RequestBody @Valid RequestModerationRequest requestModerationRequest) {
        log.debug(String.format("PATCH /users/%d/events/%d/requests, body=%s", userId, eventId, requestModerationRequest));
        return requestMapper.toModerationResult(requestService.updateRequestStatus(userId, eventId, requestModerationRequest));
    }

    @GetMapping("/{userId}/events/{eventId}/comments")
    public List<CommentDto> getCommentsByEvent(@PathVariable long userId,
                                               @PathVariable long eventId,
                                               @RequestParam(required = false) CommentState status) {
        log.debug(String.format("GET /users/%d/events/%d/comments, params: status=%s", userId, eventId, status));
        return commentMapper.toDto(commentService.getCommentsByEventId(eventId, userId, status));
    }

    @GetMapping("/{userId}/comments")
    public List<CommentDto> getCommentsByUserId(@PathVariable long userId,
                                                @RequestParam(required = false) CommentState status) {
        log.debug(String.format("GET /users/%d/comments, params: status=%s", userId, status));
        return commentMapper.toDto(commentService.getCommentsByUserId(userId, status));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{userId}/comments")
    public CommentDto addComment(@PathVariable long userId,
                                 @RequestParam long eventId,
                                 @RequestBody @Valid CommentRequest commentRequest) {
        log.debug(String.format("POST /users/%d/comments, params: eventId=%d, body=%s", userId, eventId, commentRequest));
        Event event = eventService.getEventById(eventId);
        User author = userService.getUserById(userId);
        return commentMapper.toDto(commentService.addComment(commentMapper.toComment(commentRequest, author, event)));
    }

    @PatchMapping("/{userId}/events/{eventId}/comments")
    public CommentModerationResult updateCommentStatus(@PathVariable long userId,
                                                       @PathVariable long eventId,
                                                       @RequestBody @Valid CommentModerationRequest commentModerationRequest) {
        log.debug(String.format("PATCH /users/%d/events/%d/comments, body=%s", userId, eventId, commentModerationRequest));
        return commentMapper.toModerationResult(commentService.updateCommentStatus(userId, eventId, commentModerationRequest));
    }

    @PatchMapping("/{userId}/comments/{commentId}")
    public CommentDto updateComment(@PathVariable long userId,
                                    @PathVariable long commentId,
                                    @RequestBody @Valid CommentRequest commentRequest) {
        log.debug(String.format("PATCH /users/%d/comments/%d, body=%s", userId, commentId, commentRequest));
        return commentMapper.toDto(commentService.updateComment(userId, commentId, commentRequest));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{userId}/comments/{commentId}")
    public void deleteComment(@PathVariable long userId,
                              @PathVariable long commentId) {
        log.debug(String.format("DELETE /users/%d/comments/%d", userId, commentId));
        commentService.deleteUserComment(userId, commentId);
    }
}