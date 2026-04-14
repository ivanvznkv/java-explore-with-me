package ru.practicum.ewm.event.repository;

import ru.practicum.ewm.event.model.Event;
import java.time.LocalDateTime;
import java.util.List;

public interface EventRepositoryCustom {
    List<Event> searchEventsAdmin(List<Long> users, List<String> states, List<Long> categories,
                                  LocalDateTime rangeStart, LocalDateTime rangeEnd, int offset, int size);

    List<Event> searchEventsPublic(String text, List<Long> categories, Boolean paid,
                                   LocalDateTime rangeStart, LocalDateTime rangeEnd, int offset, int size);
}