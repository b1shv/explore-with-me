package ru.practicum.service;

import ru.practicum.model.Event;

import java.util.List;
import java.util.Map;

public interface StatsService {
    long getEventViews(Event event);

    Map<Long, Long> getEventsViews(List<Event> events);

    void hit(String uri, String ip);
}
