package ru.practicum.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.Location;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.model.state.CommentState;
import ru.practicum.model.state.event.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventMapper {
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;

    public Event toEvent(EventDto eventDto, Category category, User initiator) {
        return Event.builder()
                .title(eventDto.getTitle())
                .annotation(eventDto.getAnnotation())
                .description(eventDto.getDescription())
                .category(category)
                .initiator(initiator)
                .eventDate(eventDto.getEventDate())
                .paid(Boolean.TRUE.equals(eventDto.getPaid()))
                .createdOn(LocalDateTime.now())
                .latitude(eventDto.getLocation().getLat())
                .longitude(eventDto.getLocation().getLon())
                .state(EventState.PENDING)
                .participantLimit(eventDto.getParticipantLimit() == null ? 0 : eventDto.getParticipantLimit())
                .requestModeration(eventDto.getRequestModeration() == null || eventDto.getRequestModeration())
                .commentModeration(eventDto.getCommentModeration() != null && eventDto.getCommentModeration())
                .build();
    }

    public EventFullDto toFullDto(Event event) {
        EventFullDto eventFullDto = EventFullDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .category(categoryMapper.toDto(event.getCategory()))
                .initiator(userMapper.toShortDto(event.getInitiator()))
                .eventDate(event.getEventDate())
                .paid(event.isPaid())
                .createdOn(event.getCreatedOn())
                .location(new Location(event.getLatitude(), event.getLongitude()))
                .state(event.getState())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .commentModeration(event.getCommentModeration())
                .publishedOn(event.getPublishedOn())
                .confirmedRequests(event.getConfirmedRequests())
                .build();

        if (event.getComments() != null) {
            eventFullDto.setComments(event.getComments().stream()
                    .filter(comment -> comment.getStatus().equals(CommentState.PUBLISHED))
                    .map(commentMapper::toDto)
                    .collect(Collectors.toList()));
        }
        return eventFullDto;
    }

    public EventFullDto toFullDto(Event event, long views) {
        EventFullDto eventFullDto = toFullDto(event);
        eventFullDto.setViews(views);
        return eventFullDto;
    }

    public List<EventFullDto> toFullDto(List<Event> events, Map<Long, Long> views) {
        return events.stream()
                .map(event -> toFullDto(event, views.containsKey(event.getId()) ? views.get(event.getId()) : 0))
                .collect(Collectors.toList());
    }

    public EventShortDto toShortDto(Event event, long views) {
        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .category(categoryMapper.toDto(event.getCategory()))
                .initiator(userMapper.toShortDto(event.getInitiator()))
                .eventDate(event.getEventDate())
                .paid(event.isPaid())
                .views(views)
                .confirmedRequests(event.getConfirmedRequests())
                .build();
    }

    public List<EventShortDto> toShortDto(List<Event> events, Map<Long, Long> views) {
        return events.stream()
                .map(event -> toShortDto(event, views.containsKey(event.getId()) ? views.get(event.getId()) : 0))
                .collect(Collectors.toList());
    }
}
