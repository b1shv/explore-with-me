package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.StatsDto;
import ru.practicum.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {
    @Query("select new ru.practicum.StatsDto(h.app, h.uri, " +
            "case :unique " +
            "when ('true') then count(distinct h.ip) " +
            "else count(*) " +
            "end " +
            "as hits) " +
            "from Hit as h " +
            "where h.timestamp between :start and :end " +
            "group by h.app, h.uri " +
            "order by hits desc")
    List<StatsDto> findStats(LocalDateTime start, LocalDateTime end, boolean unique);

    @Query("select new ru.practicum.StatsDto(h.app, h.uri, " +
            "case :unique " +
            "when ('true') then count(distinct h.ip) " +
            "else count(*) " +
            "end " +
            "as hits) " +
            "from Hit as h " +
            "where h.timestamp between :start and :end " +
            "and h.uri in :uris " +
            "group by h.app, h.uri " +
            "order by hits desc")
    List<StatsDto> findStatsForUris(LocalDateTime start, LocalDateTime end, boolean unique, String[] uris);
}
