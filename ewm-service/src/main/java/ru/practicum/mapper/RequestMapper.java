package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestUpdateResult;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.state.RequestState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RequestMapper {
    public ParticipationRequestDto toDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .event(request.getEventId())
                .requester(request.getRequesterId())
                .status(request.getStatus())
                .created(request.getCreated())
                .build();
    }

    public ParticipationRequest createRequest(long eventId, long userId) {
        return ParticipationRequest.builder()
                .eventId(eventId)
                .requesterId(userId)
                .status(RequestState.PENDING)
                .created(LocalDateTime.now())
                .build();
    }

    public List<ParticipationRequestDto> toDto(List<ParticipationRequest> requests) {
        return requests.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public RequestUpdateResult toUpdateResult(List<ParticipationRequest> requests) {
        RequestUpdateResult updateResult = new RequestUpdateResult();
        requests.stream()
                .map(this::toDto)
                .forEach(request -> {
                    if (request.getStatus().equals(RequestState.CONFIRMED)) {
                        updateResult.addConfirmedRequest(request);
                    } else {
                        updateResult.addRejectedRequest(request);
                    }
                });
        return updateResult;
    }
}
