package ru.practicum.ewm.event.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.event.model.Event;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class EventRepositoryImpl implements EventRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Event> searchEventsAdmin(List<Long> users, List<String> states, List<Long> categories,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd, int offset, int size) {
        String sql = """
                SELECT DISTINCT e.* FROM events e
                WHERE (:users IS NULL OR e.initiator_id = ANY(:users))
                AND (:states IS NULL OR e.state = ANY(:states))
                AND (:categories IS NULL OR e.category_id = ANY(:categories))
                AND e.event_date >= COALESCE(:rangeStart, '1900-01-01'::timestamp)
                AND e.event_date <= COALESCE(:rangeEnd, '3000-01-01'::timestamp)
                ORDER BY e.id
                OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY
                """;

        Query query = entityManager.createNativeQuery(sql, Event.class);
        query.setParameter("users", createArray("bigint", users));
        query.setParameter("states", createArray("varchar", states));
        query.setParameter("categories", createArray("bigint", categories));
        query.setParameter("rangeStart", rangeStart);
        query.setParameter("rangeEnd", rangeEnd);
        query.setParameter("offset", offset);
        query.setParameter("size", size);

        return query.getResultList();
    }

    @Override
    public List<Event> searchEventsPublic(String text, List<Long> categories, Boolean paid,
                                          LocalDateTime rangeStart, LocalDateTime rangeEnd, int offset, int size) {
        String sql = """
                SELECT DISTINCT e.* FROM events e
                WHERE e.state = 'PUBLISHED'
                AND (:text IS NULL OR :text = '' OR
                     LOWER(e.annotation::text) LIKE LOWER(CONCAT('%', :text, '%')) OR
                     LOWER(e.description::text) LIKE LOWER(CONCAT('%', :text, '%')))
                AND (:categories IS NULL OR e.category_id = ANY(:categories))
                AND (:paid IS NULL OR e.paid = :paid)
                AND e.event_date >= COALESCE(:rangeStart, '1900-01-01'::timestamp)
                AND e.event_date <= COALESCE(:rangeEnd, '3000-01-01'::timestamp)
                ORDER BY e.event_date
                OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY
                """;

        Query query = entityManager.createNativeQuery(sql, Event.class);
        query.setParameter("text", text);
        query.setParameter("categories", createArray("bigint", categories));
        query.setParameter("paid", paid);
        query.setParameter("rangeStart", rangeStart);
        query.setParameter("rangeEnd", rangeEnd);
        query.setParameter("offset", offset);
        query.setParameter("size", size);

        return query.getResultList();
    }

    private java.sql.Array createArray(String typeName, List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            Connection connection = entityManager.unwrap(Session.class)
                    .doReturningWork(conn -> conn);
            return connection.createArrayOf(typeName, list.toArray());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create SQL array", e);
        }
    }
}