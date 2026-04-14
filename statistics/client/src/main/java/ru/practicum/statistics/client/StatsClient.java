package ru.practicum.statistics.client;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.statistics.dto.EndpointHit;
import ru.practicum.statistics.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class StatsClient {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate rest;
    private final String serverUrl;
    private final String appName;

    public StatsClient(@Value("${stats-server.url:http://localhost:9090}") String serverUrl,
                       @Value("${stats.app-name:ewm-main-service}") String appName,
                       RestTemplateBuilder builder) {
        this.serverUrl = serverUrl;
        this.appName = appName;
        this.rest = builder.build();
    }

    public void sendHit(EndpointHit hit) {
        HttpEntity<EndpointHit> requestEntity = new HttpEntity<>(hit);
        rest.exchange(serverUrl + "/hit", HttpMethod.POST, requestEntity, Void.class);
    }

    public void sendHit(HttpServletRequest request) {
        EndpointHit hit = new EndpointHit();
        hit.setApp(appName);
        hit.setUri(request.getRequestURI());
        hit.setIp(request.getRemoteAddr());
        hit.setTimestamp(LocalDateTime.now());
        sendHit(hit);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        String startStr = start.format(DATE_TIME_FORMATTER);
        String endStr = end.format(DATE_TIME_FORMATTER);

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
        ViewStats[] body = response.getBody();
        return body != null ? Arrays.asList(body) : Collections.emptyList();
    }
}