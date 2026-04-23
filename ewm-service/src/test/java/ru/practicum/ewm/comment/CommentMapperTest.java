package ru.practicum.ewm.comment;

import org.junit.jupiter.api.Test;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.model.CommentStatus;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {

    @Test
    void toComment_ShouldMapCorrectly() {
        String text = "Тестовый комментарий";

        User author = new User(1L, "test", "test@example.com");
        Category category = new Category(1L, "Концерты");
        Event event = new Event();
        event.setId(1L);
        event.setAnnotation("Аннотация");
        event.setDescription("Описание");
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setLocation(new Location(55.75f, 37.62f));
        event.setPaid(false);
        event.setParticipantLimit(0);
        event.setRequestModeration(true);
        event.setTitle("Событие");
        event.setCategory(category);
        event.setInitiator(author);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PUBLISHED);

        Comment comment = CommentMapper.toComment(text, author, event);

        assertEquals(text, comment.getText());
        assertEquals(author, comment.getAuthor());
        assertEquals(event, comment.getEvent());
        assertEquals(CommentStatus.PUBLISHED, comment.getStatus());
        assertNotNull(comment.getCreatedOn());
        assertNull(comment.getEditedOn());
    }

    @Test
    void toCommentDto_ShouldMapAllFields() {
        User author = new User(1L, "test", "test@example.com");
        Category category = new Category(1L, "Концерты");
        Event event = new Event();
        event.setId(2L);

        Comment comment = new Comment();
        comment.setId(10L);
        comment.setText("Текст");
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setCreatedOn(LocalDateTime.now().minusHours(1));
        comment.setEditedOn(LocalDateTime.now());
        comment.setStatus(CommentStatus.PUBLISHED);

        CommentDto dto = CommentMapper.toCommentDto(comment);

        assertEquals(10L, dto.getId());
        assertEquals("Текст", dto.getText());
        assertEquals(1L, dto.getAuthor().getId());
        assertEquals(2L, dto.getEventId());
        assertEquals("PUBLISHED", dto.getStatus());
        assertNotNull(dto.getCreatedOn());
        assertNotNull(dto.getEditedOn());
    }
}