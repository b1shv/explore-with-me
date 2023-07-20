package ru.practicum;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class StatsClient {
    private final RestTemplate restTemplate;

    public StatsClient(String statsUrl, RestTemplateBuilder builder) {
        this.restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(statsUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public void hit(HitDto hitDto) {
        restTemplate.postForEntity("/hit", hitDto, HitDto.class);
    }

    public StatsDto[] getStats(LocalDateTime start, LocalDateTime end) {
        return getStats(start, end, false, null);
    }

    public StatsDto[] getStats(LocalDateTime start, LocalDateTime end, boolean unique) {
        return getStats(start, end, unique, null);
    }

    public StatsDto[] getStats(LocalDateTime start, LocalDateTime end, String[] uris) {
        return getStats(start, end, false, uris);
    }

    public StatsDto[] getStats(LocalDateTime start, LocalDateTime end, boolean unique, String[] uris) {
        StringBuilder url = new StringBuilder("/stats?start={start}&end={end}");
        Map<String, Object> params = new HashMap<>();
        params.put("start", start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        params.put("end", end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        if (unique) {
            params.put("unique", true);
            url.append("&unique={unique}");
        }
        if (uris != null && uris.length > 0) {
            params.put("uris", uris);
            url.append("&uris={uris}");
        }

        ResponseEntity<StatsDto[]> responseEntity = restTemplate.getForEntity(url.toString(), StatsDto[].class, params);
        return responseEntity.getBody();
    }
}
