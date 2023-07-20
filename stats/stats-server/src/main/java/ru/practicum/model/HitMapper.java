package ru.practicum.model;

import org.springframework.stereotype.Component;
import ru.practicum.HitDto;

@Component
public class HitMapper {
    public Hit toHit(HitDto hitDto) {
        return Hit.builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamp(hitDto.getTimestamp())
                .build();
    }
}
