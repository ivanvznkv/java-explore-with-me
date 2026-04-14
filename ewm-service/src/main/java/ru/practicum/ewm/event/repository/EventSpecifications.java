package ru.practicum.ewm.event.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;

import jakarta.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.List;

public class EventSpecifications {

    public static Specification<Event> hasInitiators(List<Long> users) {
        return (root, query, cb) -> {
            if (users == null || users.isEmpty()) return null;
            return root.get("initiator").get("id").in(users);
        };
    }

    public static Specification<Event> hasStates(List<String> stateStrings) {
        return (root, query, cb) -> {
            if (stateStrings == null || stateStrings.isEmpty()) return null;
            List<EventState> states = stateStrings.stream()
                    .map(EventState::valueOf)
                    .toList();
            return root.get("state").in(states);
        };
    }

    public static Specification<Event> hasCategories(List<Long> categories) {
        return (root, query, cb) -> {
            if (categories == null || categories.isEmpty()) return null;
            return root.get("category").get("id").in(categories);
        };
    }

    public static Specification<Event> eventDateBetween(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        return (root, query, cb) -> {
            if (rangeStart == null && rangeEnd == null) return null;
            Path<LocalDateTime> eventDate = root.get("eventDate");
            if (rangeStart != null && rangeEnd != null) {
                return cb.between(eventDate, rangeStart, rangeEnd);
            } else if (rangeStart != null) {
                return cb.greaterThanOrEqualTo(eventDate, rangeStart);
            } else {
                return cb.lessThanOrEqualTo(eventDate, rangeEnd);
            }
        };
    }

    public static Specification<Event> isPublished() {
        return (root, query, cb) -> cb.equal(root.get("state"), EventState.PUBLISHED);
    }

    public static Specification<Event> textContains(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isBlank()) return null;
            String pattern = "%" + text.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("annotation")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Event> paidEquals(Boolean paid) {
        return (root, query, cb) -> paid == null ? null : cb.equal(root.get("paid"), paid);
    }
}