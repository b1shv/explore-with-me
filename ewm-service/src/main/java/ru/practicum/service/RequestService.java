package ru.practicum.service;

import ru.practicum.dto.request.RequestModerationRequest;
import ru.practicum.model.ParticipationRequest;

import java.util.List;

public interface RequestService {
    List<ParticipationRequest> getRequestsByUserId(long userId);

    List<ParticipationRequest> getRequestsByEventId(long userId, long eventId);

    List<ParticipationRequest> updateRequestStatus(long userId, long eventId, RequestModerationRequest requestModerationRequest);

    ParticipationRequest addRequest(ParticipationRequest request);

    ParticipationRequest cancelRequest(long userId, long requestId);
}
