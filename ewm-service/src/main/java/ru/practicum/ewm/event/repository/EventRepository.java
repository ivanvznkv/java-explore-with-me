package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventRepository extends JpaRepository<Event, Long> {

    boolean existsByCategoryId(Long categoryId);

    @Query(value = "SELECT DISTINCT e.* FROM events e " +
            "LEFT JOIN categories c ON c.id = e.category_id " +
            "LEFT JOIN users u ON u.id = e.initiator_id " +
            "WHERE (COALESCE(cast(:users AS bigint[]), ARRAY[]::bigint[]) = ARRAY[]::bigint[] OR e.initiator_id IN (:users)) " +
            "AND (COALESCE(cast(:states AS varchar[]), ARRAY[]::varchar[]) = ARRAY[]::varchar[] OR e.state IN (:states)) " +
            "AND (COALESCE(cast(:categories AS bigint[]), ARRAY[]::bigint[]) = ARRAY[]::bigint[] OR e.category_id IN (:categories)) " +
            "AND e.event_date >= COALESCE(:rangeStart, '1900-01-01'::timestamp) " +
            "AND e.event_date <= COALESCE(:rangeEnd, '3000-01-01'::timestamp) " +
            "OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY", nativeQuery = true)
    List<Event> searchEventsAdmin(@Param("users") List<Long> users,
                                  @Param("states") List<String> states,
                                  @Param("categories") List<Long> categories,
                                  @Param("rangeStart") LocalDateTime rangeStart,
                                  @Param("rangeEnd") LocalDateTime rangeEnd,
                                  @Param("offset") int offset,
                                  @Param("size") int size);

    @Query(value = "SELECT DISTINCT e.* FROM events e " +
            "LEFT JOIN categories c ON c.id = e.category_id " +
            "LEFT JOIN users u ON u.id = e.initiator_id " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (COALESCE(:text, '') = '' OR " +
            "     LOWER(e.annotation::text) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "     LOWER(e.description::text) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND (COALESCE(cast(:categories AS bigint[]), ARRAY[]::bigint[]) = ARRAY[]::bigint[] OR e.category_id IN (:categories)) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND e.event_date >= COALESCE(:rangeStart, '1900-01-01'::timestamp) " +
            "AND e.event_date <= COALESCE(:rangeEnd, '3000-01-01'::timestamp) " +
            "OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY", nativeQuery = true)
    List<Event> searchEventsPublic(@Param("text") String text,
                                   @Param("categories") List<Long> categories,
                                   @Param("paid") Boolean paid,
                                   @Param("rangeStart") LocalDateTime rangeStart,
                                   @Param("rangeEnd") LocalDateTime rangeEnd,
                                   @Param("offset") int offset,
                                   @Param("size") int size);

    @Query("SELECT COUNT(r) FROM Request r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    Long countConfirmedRequests(@Param("eventId") Long eventId);

    @Query("SELECT r.event.id, COUNT(r) FROM Request r WHERE r.event.id IN :eventIds AND r.status = 'CONFIRMED' GROUP BY r.event.id")
    Map<Long, Long> countConfirmedRequestsBatch(@Param("eventIds") List<Long> eventIds);

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.category LEFT JOIN FETCH e.initiator WHERE e.initiator.id = :initiatorId")
    List<Event> findAllByInitiatorIdWithDetails(@Param("initiatorId") Long initiatorId, Pageable pageable);
}