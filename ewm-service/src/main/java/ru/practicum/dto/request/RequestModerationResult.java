package ru.practicum.dto.request;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RequestModerationResult {
    private final List<ParticipationRequestDto> confirmedRequests;
    private final List<ParticipationRequestDto> rejectedRequests;

    public RequestModerationResult() {
        this.confirmedRequests = new ArrayList<>();
        this.rejectedRequests = new ArrayList<>();
    }

    public void addConfirmedRequest(ParticipationRequestDto request) {
        confirmedRequests.add(request);
    }

    public void addRejectedRequest(ParticipationRequestDto request) {
        rejectedRequests.add(request);
    }
}
