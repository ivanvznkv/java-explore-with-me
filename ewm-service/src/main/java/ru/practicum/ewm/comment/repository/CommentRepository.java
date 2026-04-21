package ru.practicum.ewm.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.model.CommentStatus;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"author", "event"})
    Page<Comment> findAllByEventIdAndStatus(Long eventId, CommentStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "event"})
    Page<Comment> findAllByAuthorId(Long authorId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "event"})
    @Query("SELECT c FROM Comment c " +
            "WHERE (:eventId IS NULL OR c.event.id = :eventId) " +
            "AND (:authorId IS NULL OR c.author.id = :authorId) " +
            "AND (:status IS NULL OR c.status = :status)")
    Page<Comment> searchAdmin(@Param("eventId") Long eventId,
                              @Param("authorId") Long authorId,
                              @Param("status") CommentStatus status,
                              Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"author", "event"})
    Optional<Comment> findById(Long id);
}