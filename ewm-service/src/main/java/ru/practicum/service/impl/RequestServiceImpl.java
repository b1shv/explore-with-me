package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.request.RequestModerationRequest;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ObjectUpdateForbiddenException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.QParticipationRequest;
import ru.practicum.model.state.RequestState;
import ru.practicum.model.state.RequestUpdateState;
import ru.practicum.model.state.event.EventState;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.RequestService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<ParticipationRequest> getRequestsByUserId(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id=%d is not found", userId));
        }
        return toList(requestRepository.findAll(QParticipationRequest.participationRequest.requesterId.eq(userId)));
    }

    @Override
    public List<ParticipationRequest> getRequestsByEventId(long userId, long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id=%d is not found", userId));
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d is not found", eventId)));
        if (event.getInitiator().getId() != userId) {
            throw new AccessDeniedException(
                    String.format("User with id=%d is not an initiator of event with id=%d", userId, eventId));
        }
        return toList(requestRepository.findAll(QParticipationRequest.participationRequest.eventId.eq(eventId)));
    }

    @Override
    public ParticipationRequest addRequest(ParticipationRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d is not found", request.getEventId())));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new AccessDeniedException(String.format("Event with id=%d is not published", request.getEventId()));
        }
        if (request.getRequesterId() == event.getInitiator().getId()) {
            throw new AccessDeniedException(String.format("User with id=%d is an initiator of event with id=%d",
                    request.getRequesterId(), request.getEventId()));
        }
        if (event.getParticipantLimit() != 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new AccessDeniedException("The participant limit has been reached");
        }
        if (Boolean.TRUE.equals(!event.getRequestModeration()) || event.getParticipantLimit() == 0) {
            request.setStatus(RequestState.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        return requestRepository.save(request);
    }

    @Override
    public ParticipationRequest cancelRequest(long userId, long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Request with id=%d is not found", requestId)));
        if (request.getRequesterId() != userId) {
            throw new AccessDeniedException(String.format(
                    "User with id=%d is not an owner of request with id=%d", userId, requestId));
        }
        if (request.getStatus().equals(RequestState.CONFIRMED)) {
            eventRepository.findById(request.getEventId()).ifPresent(event -> {
                event.setConfirmedRequests(event.getConfirmedRequests() - 1);
                eventRepository.save(event);
            });
        }

        request.setStatus(RequestState.CANCELED);
        return requestRepository.save(request);
    }

    @Override
    public List<ParticipationRequest> updateRequestStatus(long userId, long eventId, RequestModerationRequest requestModerationRequest) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id=%d is not found", userId));
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d is not found", eventId)));

        if (event.getInitiator().getId() != userId) {
            throw new AccessDeniedException(
                    String.format("User with id=%d is not an initiator of event with id=%d", userId, eventId));
        }
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ValidationException("Moderation for event requests is disabled");
        }

        List<ParticipationRequest> requests = toList(
                requestRepository.findAll(QParticipationRequest.participationRequest.id.in(requestModerationRequest.getRequestIds())));

        if (requests.stream().anyMatch(request -> !request.getStatus().equals(RequestState.PENDING))) {
            throw new ObjectUpdateForbiddenException("Request must have status PENDING");
        }
        if (requestModerationRequest.getStatus().equals(RequestUpdateState.CONFIRMED)) {
            if (event.getParticipantLimit() < event.getConfirmedRequests() + requestModerationRequest.getRequestIds().size()) {
                throw new ObjectUpdateForbiddenException("The participant limit has been reached");
            }
            event.setConfirmedRequests(event.getConfirmedRequests() + requests.size());
            eventRepository.save(event);
        }

        requests.forEach(request -> request.setStatus(
                requestModerationRequest.getStatus().equals(RequestUpdateState.CONFIRMED) ? RequestState.CONFIRMED : RequestState.REJECTED));
        return requestRepository.saveAll(requests);
    }

    private List<ParticipationRequest> toList(Iterable<ParticipationRequest> iterable) {
        List<ParticipationRequest> requests = new ArrayList<>();
        iterable.forEach(requests::add);
        return requests;
    }
}
