package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.util.SortType;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.statistics.client.StatsClient;
import ru.practicum.statistics.dto.EndpointHit;
import ru.practicum.statistics.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicEventServiceImpl implements PublicEventService {
    private final EventRepository eventRepository;
    private final StatsClient statsClient;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final LocalDateTime STATS_START = LocalDateTime.of(2000, 1, 1, 0, 0, 0);

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                         Boolean onlyAvailable, SortType sort,
                                         int from, int size, HttpServletRequest request) {
        sendHit(request);

        LocalDateTime now = LocalDateTime.now();
        if (rangeStart == null) rangeStart = now;
        if (rangeEnd == null) rangeEnd = LocalDateTime.of(2999, 12, 31, 23, 59, 59);

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findPublishedEventsWithFilters(
                text, categories, paid, rangeStart, rangeEnd, pageable);

        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(events);

        if (onlyAvailable) {
            events = events.stream()
                    .filter(event -> {
                        long confirmed = confirmedRequestsMap.getOrDefault(event.getId(), 0L);
                        int limit = event.getParticipantLimit();
                        return limit == 0 || confirmed < limit;
                    })
                    .collect(Collectors.toList());
        }

        Map<Long, Long> viewsMap = getViewsMap(events);

        List<EventShortDto> result = events.stream()
                .map(event -> EventMapper.toEventShortDto(event,
                        viewsMap.getOrDefault(event.getId(), 0L),
                        confirmedRequestsMap.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());

        if (sort == SortType.VIEWS) {
            result.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        } else if (sort == SortType.EVENT_DATE) {
            result.sort(Comparator.comparing(EventShortDto::getEventDate));
        }

        if (sort == SortType.VIEWS) {
            int endIndex = Math.min(from + size, result.size());
            result = result.subList(from, endIndex);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long eventId, HttpServletRequest request) {
        sendHit(request);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с id=" + eventId + " ещё не опубликовано");
        }
        Map<Long, Long> viewsMap = getViewsMap(List.of(event));
        Long views = viewsMap.getOrDefault(eventId, 0L);
        Long confirmed = eventRepository.countConfirmedRequests(eventId);
        return EventMapper.toEventFullDto(event, views, confirmed);
    }

    private void sendHit(HttpServletRequest request) {
        try {
            EndpointHit hit = new EndpointHit();
            hit.setApp("ewm-main-service");
            hit.setUri(request.getRequestURI());
            hit.setIp(request.getRemoteAddr());
            hit.setTimestamp(LocalDateTime.now());
            statsClient.sendHit(hit);
        } catch (Exception e) {
            log.error("Ошибка при отправке статистики: {}", e.getMessage());
        }
    }

    private Map<Long, Long> getViewsMap(List<Event> events) {
        if (events.isEmpty()) return Map.of();
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());
        try {
            List<ViewStats> stats = statsClient.getStats(STATS_START, LocalDateTime.now().plusDays(1), uris, false);
            return stats.stream()
                    .collect(Collectors.toMap(
                            vs -> Long.parseLong(vs.getUri().substring(vs.getUri().lastIndexOf('/') + 1)),
                            ViewStats::getHits));
        } catch (Exception e) {
            log.error("Ошибка получения статистики просмотров: {}", e.getMessage());
            return events.stream().collect(Collectors.toMap(Event::getId, event -> 0L));
        }
    }

    private Map<Long, Long> getConfirmedRequestsMap(List<Event> events) {
        if (events.isEmpty()) return Map.of();
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        return eventRepository.countConfirmedRequestsBatch(eventIds);
    }
}