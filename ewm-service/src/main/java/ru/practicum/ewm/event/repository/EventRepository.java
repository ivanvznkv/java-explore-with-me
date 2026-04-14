package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.model.Event;

import java.util.List;
import java.util.Map;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    boolean existsByCategoryId(Long categoryId);

    @Query("SELECT COUNT(r) FROM Request r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    Long countConfirmedRequests(@Param("eventId") Long eventId);

    @Query("SELECT r.event.id, COUNT(r) FROM Request r WHERE r.event.id IN :eventIds AND r.status = 'CONFIRMED' GROUP BY r.event.id")
    Map<Long, Long> countConfirmedRequestsBatch(@Param("eventIds") List<Long> eventIds);

    @Query("SELECT e FROM Event e " +
            "LEFT JOIN FETCH e.category " +
            "LEFT JOIN FETCH e.initiator " +
            "WHERE e.initiator.id = :initiatorId " +
            "ORDER BY e.id")
    List<Event> findAllByInitiatorIdWithDetails(@Param("initiatorId") Long initiatorId, Pageable pageable);
}