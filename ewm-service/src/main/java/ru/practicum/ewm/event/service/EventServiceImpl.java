package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.util.SortType;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.statistics.client.StatsClient;
import ru.practicum.statistics.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Дата события должна быть не ранее чем через 2 часа от текущего момента");
        }
        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id=" + dto.getCategory() + " не найдена"));
        Event event = EventMapper.toEvent(dto, category, initiator);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event = eventRepository.save(event);
        return EventMapper.toEventFullDto(event, 0L, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        int page = from / size;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id"));
        List<Event> events = eventRepository.findAllByInitiatorIdWithDetails(userId, pageRequest);
        return events.stream()
                .map(event -> EventMapper.toEventShortDto(event, 0L, 0L))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие не принадлежит пользователю");
        }
        return EventMapper.toEventFullDto(event, 0L, 0L);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие не принадлежит пользователю");
        }
        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Можно изменять только события в статусе PENDING или CANCELED");
        }
        if (request.getEventDate() != null && request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Дата события должна быть не ранее чем через 2 часа от текущего момента");
        }
        EventMapper.updateEventFromUserRequest(request, event);
        if (request.getStateAction() != null) {
            if (request.getStateAction().equals("SEND_TO_REVIEW")) {
                event.setState(EventState.PENDING);
            } else if (request.getStateAction().equals("CANCEL_REVIEW")) {
                event.setState(EventState.CANCELED);
            }
        }
        event = eventRepository.save(event);
        return EventMapper.toEventFullDto(event, 0L, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEventsForAdmin(AdminEventSearchParams params) {
        int from = params.getFrom();
        int size = params.getSize();
        if (size <= 0 || from < 0) {
            throw new IllegalArgumentException("Параметры from и size должны быть > 0");
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id"));
        Page<Event> page = eventRepository.searchEventsAdmin(
                params.getUsers(),
                params.getStates(),
                params.getCategories(),
                params.getRangeStart(),
                params.getRangeEnd(),
                pageable);

        List<Event> events = page.getContent();
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedRequestsMap = eventRepository.countConfirmedRequestsBatch(eventIds);

        return events.stream()
                .map(event -> EventMapper.toEventFullDto(event, 0L, confirmedRequestsMap.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (request.getStateAction() != null) {
            if (request.getStateAction().equals("PUBLISH_EVENT")) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Событие можно публиковать только в статусе PENDING");
                }
                if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                    throw new ConflictException("Дата начала события должна быть не ранее чем за час от текущего момента");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (request.getStateAction().equals("REJECT_EVENT")) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Нельзя отклонить уже опубликованное событие");
                }
                event.setState(EventState.CANCELED);
            }
        }

        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());

        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException("Дата события не может быть раньше чем через 2 часа от текущего момента");
            }
            event.setEventDate(request.getEventDate());
        }

        if (request.getCategory() != null) {
            Category newCategory = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + request.getCategory() + " не найдена"));
            event.setCategory(newCategory);
        }

        if (request.getLocation() != null) event.setLocation(request.getLocation());

        event = eventRepository.save(event);
        Long views = 0L;
        Long confirmed = eventRepository.countConfirmedRequests(eventId);
        return EventMapper.toEventFullDto(event, views, confirmed);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsForPublic(PublicEventSearchParams params, HttpServletRequest request) {
        int from = params.getFrom();
        int size = params.getSize();
        if (size <= 0 || from < 0) {
            throw new IllegalArgumentException("Параметры from и size должны быть > 0");
        }

        try {
            statsClient.sendHit(request);
        } catch (Exception e) {
            log.warn("Не удалось отправить статистику: {}", e.getMessage());
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("eventDate").ascending());
        Page<Event> page = eventRepository.searchEventsPublic(
                params.getText(),
                params.getCategories(),
                params.getPaid(),
                params.getRangeStart(),
                params.getRangeEnd(),
                pageable);

        List<Event> events = page.getContent();
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(events);

        if (params.getOnlyAvailable()) {
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

        SortType sort = params.getSort();
        if (sort == SortType.VIEWS) {
            result.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        } else if (sort == SortType.EVENT_DATE) {
            result.sort(Comparator.comparing(EventShortDto::getEventDate));
        }

        return result;
    }


    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventForPublic(Long eventId, HttpServletRequest request) {
        try {
            statsClient.sendHit(request);
        } catch (Exception e) {
            log.warn("Не удалось отправить статистику: {}", e.getMessage());
        }

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

    private Map<Long, Long> getViewsMap(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        LocalDateTime minDate = events.stream()
                .map(event -> event.getPublishedOn() != null ? event.getPublishedOn() : event.getCreatedOn())
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusDays(1));

        LocalDateTime start = minDate;
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        try {
            List<ViewStats> stats = statsClient.getStats(start, end, uris, true);
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