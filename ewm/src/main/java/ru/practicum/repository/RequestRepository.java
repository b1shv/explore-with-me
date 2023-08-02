package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.RequestsCounter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long>, QuerydslPredicateExecutor<ParticipationRequest> {
    @Query(value =
            "select new ru.practicum.model.RequestsCounter(eventId, count(*)) " +
                    "from ParticipationRequest " +
                    "where eventId in :eventsIds and status = 'APPROVED' " +
                    "group by eventId")
    List<RequestsCounter> countRequestsList(List<Long> eventsIds);

    default Map<Long, Long> countRequests(List<Long> eventsIds) {
        return countRequestsList(eventsIds).stream()
                .collect(Collectors.toMap(RequestsCounter::getEventId, RequestsCounter::getRequests));
    }
}
