package ru.practicum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatsConfig {
    @Value("${stats-server.url}")
    private String statsServerUrl;

    @Bean
    StatsClient statsClient() {
        return new StatsClient(statsServerUrl, new RestTemplateBuilder());
    }
}
