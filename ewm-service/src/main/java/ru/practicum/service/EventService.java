package ru.practicum.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.controller.params.SearchParamsAdmin;
import ru.practicum.controller.params.SearchParamsPublic;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.model.Event;

import java.util.List;

public interface EventService {
    List<Event> getAllEvents(SearchParamsAdmin params);

    List<Event> getAllEvents(SearchParamsPublic params);

    List<Event> getEventsByUserId(long userId, Pageable pageable);

    Event getPublishedEventById(long eventId);

    Event getUserEventById(long eventId, long initiatorId);

    Event getEventById(long eventId);

    List<Event> getEventsByIds(List<Long> ids);

    Event addEvent(Event event);

    Event updateEvent(long eventId, long userId, UpdateEventUserRequest eventDto);

    Event updateEvent(long eventId, UpdateEventAdminRequest eventDto);
}
