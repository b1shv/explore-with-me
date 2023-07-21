package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.HitDto;
import ru.practicum.StatsDto;
import ru.practicum.model.HitMapper;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;
    private final HitMapper hitMapper;

    @Override
    public void addHit(HitDto hitDto) {
        statsRepository.save(hitMapper.toHit(hitDto));
    }

    @Override
    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, boolean unique, String[] uris) {
        if (uris == null) {
            return statsRepository.findStats(start, end, unique);
        }
        return statsRepository.findStatsForUris(start, end, unique, uris);
    }
}
