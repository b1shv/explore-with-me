package ru.practicum.service.impl;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.controller.params.ResultSort;
import ru.practicum.controller.params.SearchParamsAdmin;
import ru.practicum.controller.params.SearchParamsPublic;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Event;
import ru.practicum.model.QEvent;
import ru.practicum.model.state.AdminStateAction;
import ru.practicum.model.state.EventState;
import ru.practicum.model.state.UserStateAction;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.CategoryService;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private static final String NOT_FOUND_MESSAGE = "Event with id=%d was not found";
    private final EventRepository eventRepository;
    private final CategoryService categoryService;

    @Override
    public List<Event> getAllEvents(SearchParamsAdmin params) {
        QEvent q = QEvent.event;
        BooleanBuilder searchConditions = new BooleanBuilder();

        Optional.ofNullable(params.getUsers()).ifPresent(
                users -> searchConditions.and(q.initiator.id.in(users)));
        Optional.ofNullable(params.getStates()).ifPresent(
                states -> searchConditions.and(q.state.in(states)));
        Optional.ofNullable(params.getCategories()).ifPresent(
                categories -> searchConditions.and(q.category.id.in(categories)));
        Optional.ofNullable(params.getRangeStart()).ifPresent(
                rangeStart -> searchConditions.and(q.eventDate.after(rangeStart)));
        Optional.ofNullable(params.getRangeEnd()).ifPresent(
                rangeEnd -> searchConditions.and(q.eventDate.before(rangeEnd)));

        return eventRepository.findAll(searchConditions, params.getPageable()).getContent();
    }

    @Override
    public List<Event> getAllEvents(SearchParamsPublic params) {
        QEvent q = QEvent.event;
        BooleanBuilder searchConditions = new BooleanBuilder(q.state.eq(EventState.PUBLISHED));

        Optional.ofNullable(params.getText()).ifPresent(
                text -> searchConditions.andAnyOf(q.annotation.toLowerCase().contains(text.toLowerCase()),
                        q.description.toLowerCase().contains(text.toLowerCase())));
        Optional.ofNullable(params.getCategories()).ifPresent(
                categories -> searchConditions.and(q.category.id.in(categories)));
        Optional.ofNullable(params.getPaid()).ifPresent(
                paid -> searchConditions.and(q.paid.eq(paid)));
        Optional.ofNullable(params.getRangeStart()).ifPresent(
                rangeStart -> searchConditions.and(q.eventDate.after(rangeStart)));
        Optional.ofNullable(params.getRangeEnd()).ifPresent(
                rangeEnd -> searchConditions.and(q.eventDate.before(rangeEnd)));

        if (params.getRangeStart() == null && params.getRangeEnd() == null) {
            searchConditions.and(q.eventDate.after(LocalDateTime.now()));
        }
        if (params.isOnlyAvailable()) {
            searchConditions.and(q.confirmedRequests.lt(q.participantLimit));
        }

        int page = params.getFrom() / params.getSize();

        if (params.getSort() != null && params.getSort().equals(ResultSort.EVENT_DATE)) {
            eventRepository.findAll(searchConditions, PageRequest.of(page, params.getSize(),
                    Sort.by(Sort.Direction.DESC, "eventDate"))).getContent();
        }
        return eventRepository.findAll(searchConditions, PageRequest.of(page, params.getSize())).getContent();
    }

    @Override
    public List<Event> getEventsByUserId(long userId, Pageable pageable) {
        return eventRepository.findAll(QEvent.event.initiator.id.eq(userId), pageable).getContent();
    }

    @Override
    public Event getPublishedEventById(long eventId) {
        return eventRepository.findOne(
                        QEvent.event.id.eq(eventId).and(QEvent.event.state.eq(EventState.PUBLISHED)))
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_MESSAGE, eventId)));
    }

    @Override
    public Event getUserEventById(long eventId, long initiatorId) {
        return eventRepository.findOne(
                        QEvent.event.initiator.id.eq(initiatorId).and(QEvent.event.id.eq(eventId)))
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_MESSAGE, eventId)));
    }

    @Override
    public List<Event> getEventsByIds(List<Long> ids) {
        if (ids == null) {
            return Collections.emptyList();
        }
        return eventRepository.findAllById(ids);
    }

    @Override
    public Event addEvent(Event event) {
        return eventRepository.save(event);
    }

    @Override
    public Event updateEvent(long eventId, long userId, UpdateEventUserRequest eventDto) {
        Event eventToUpdate = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_MESSAGE, eventId)));

        if (eventToUpdate.getInitiator().getId() != userId) {
            throw new ForbiddenException(String.format("User with id=%d can't update event with id=%d", userId, eventId));
        }
        if (eventToUpdate.getState().equals(EventState.PUBLISHED)) {
            throw new ForbiddenException(String.format("Event with id=%d has been published", eventId));
        }
        if (eventToUpdate.getState().equals(EventState.REJECTED) && eventDto.getStateAction() == null) {
            eventToUpdate.setState(EventState.PENDING);
        }
        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(UserStateAction.CANCEL_REVIEW)) {
                eventToUpdate.setState(EventState.CANCELED);
            } else {
                eventToUpdate.setState(EventState.PENDING);
            }
        }
        return eventRepository.save(updateEventFields(eventToUpdate, eventDto));
    }

    @Override
    public Event updateEvent(long eventId, UpdateEventAdminRequest eventDto) {
        Event eventToUpdate = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_MESSAGE, eventId)));
        if (eventDto.getStateAction() != null) {
            if (eventToUpdate.getState().equals(EventState.PUBLISHED)) {
                throw new ForbiddenException(String.format("Event with id=%d is already published", eventId));
            }
            if ((eventToUpdate.getState().equals(EventState.REJECTED)
                    || eventToUpdate.getState().equals(EventState.CANCELED))
                    && eventDto.getStateAction().equals(AdminStateAction.PUBLISH_EVENT)) {
                throw new ForbiddenException(String.format(
                        "Event with id=%d is can't be published because it was rejected or canceled", eventId));
            }
            if (eventToUpdate.getState().equals(EventState.PENDING)
                    && eventDto.getStateAction().equals(AdminStateAction.PUBLISH_EVENT)) {
                eventToUpdate.setState(EventState.PUBLISHED);
                eventToUpdate.setPublishedOn(LocalDateTime.now());
            }
            if (eventToUpdate.getState().equals(EventState.PENDING)
                    && eventDto.getStateAction().equals(AdminStateAction.REJECT_EVENT)) {
                eventToUpdate.setState(EventState.REJECTED);
            }
        }
        return eventRepository.save(updateEventFields(eventToUpdate, eventDto));
    }

    private Event updateEventFields(Event eventToUpdate, EventDto eventDto) {
        if (eventDto.getCategory() != 0) {
            eventToUpdate.setCategory(categoryService.getCategoryById(eventDto.getCategory()));
        }
        Optional.ofNullable(eventDto.getTitle()).ifPresent(eventToUpdate::setTitle);
        Optional.ofNullable(eventDto.getAnnotation()).ifPresent(eventToUpdate::setAnnotation);
        Optional.ofNullable(eventDto.getDescription()).ifPresent(eventToUpdate::setDescription);
        Optional.ofNullable(eventDto.getEventDate()).ifPresent(eventToUpdate::setEventDate);
        Optional.ofNullable(eventDto.getPaid()).ifPresent(eventToUpdate::setPaid);
        Optional.ofNullable(eventDto.getRequestModeration()).ifPresent(eventToUpdate::setRequestModeration);
        Optional.ofNullable(eventDto.getParticipantLimit()).ifPresent(eventToUpdate::setParticipantLimit);
        Optional.ofNullable(eventDto.getLocation()).ifPresent(location -> {
            eventToUpdate.setLatitude(location.getLat());
            eventToUpdate.setLongitude(location.getLon());
        });

        return eventToUpdate;
    }
}
