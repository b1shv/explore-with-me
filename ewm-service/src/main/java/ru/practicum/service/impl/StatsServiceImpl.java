package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.HitDto;
import ru.practicum.StatsClient;
import ru.practicum.StatsDto;
import ru.practicum.model.Event;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private static final String EVENT_PUBLIC_URI = "/events/";
    private static final String APP = "EWM";
    private final StatsClient statsClient;

    public long getEventViews(Event event) {
        StatsDto[] statsDtos = statsClient.getStats(
                event.getCreatedOn(),
                LocalDateTime.now(),
                true,
                new String[]{EVENT_PUBLIC_URI + event.getId()});
        return statsDtos.length == 0 ? 0 : statsDtos[0].getHits();
    }

    public Map<Long, Long> getEventsViews(List<Event> events) {
        HashMap<Long, Long> views = new HashMap<>();
        if (events.isEmpty()) {
            return views;
        }
        String[] uris = events.stream()
                .map(event -> EVENT_PUBLIC_URI + event.getId())
                .toArray(String[]::new);
        StatsDto[] statsDtos = statsClient.getStats(
                events.stream().min(Comparator.comparing(Event::getCreatedOn)).get().getCreatedOn(),
                LocalDateTime.now(), uris);

        if (statsDtos.length == 0) {
            return views;
        }

        for (StatsDto statsDto : statsDtos) {
            long eventId = Long.parseLong(statsDto.getUri().substring(EVENT_PUBLIC_URI.length()));
            views.put(eventId, statsDto.getHits());
        }
        return views;
    }

    public void hit(String uri, String ip) {
        statsClient.hit(HitDto.builder()
                .app(APP)
                .ip(ip)
                .uri(uri)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
