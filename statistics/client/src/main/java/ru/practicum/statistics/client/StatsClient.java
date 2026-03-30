package ru.practicum.statistics.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.statistics.dto.EndpointHit;
import ru.practicum.statistics.dto.ViewStats;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class StatsClient {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate rest;
    private final String serverUrl;

    public StatsClient(@Value("${stats-server.url:http://localhost:9090}") String serverUrl,
                       RestTemplateBuilder builder) {
        this.serverUrl = serverUrl;
        this.rest = builder.build();
    }

    public void sendHit(EndpointHit hit) {
        HttpEntity<EndpointHit> requestEntity = new HttpEntity<>(hit);
        rest.exchange(serverUrl + "/hit", HttpMethod.POST, requestEntity, Void.class);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String startStr = URLEncoder.encode(start.format(DATE_TIME_FORMATTER), StandardCharsets.UTF_8);
        String endStr = URLEncoder.encode(end.format(DATE_TIME_FORMATTER), StandardCharsets.UTF_8);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                .queryParam("start", startStr)
                .queryParam("end", endStr)
                .queryParam("unique", unique);
        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                builder.queryParam("uris", uri);
            }
        }

        ResponseEntity<ViewStats[]> response = rest.getForEntity(builder.build().toUriString(), ViewStats[].class);
        return Arrays.asList(response.getBody());
    }
}