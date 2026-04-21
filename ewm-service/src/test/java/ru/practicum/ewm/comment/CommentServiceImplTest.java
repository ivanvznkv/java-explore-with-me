package ru.practicum.ewm.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.comment.dto.*;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.model.CommentStatus;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.comment.service.CommentServiceImpl;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock private CommentRepository commentRepository;
    @Mock private UserRepository userRepository;
    @Mock private EventRepository eventRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User user;
    private Event event;
    private Comment comment;
    private NewCommentDto newCommentDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "test", "test@example.com");
        Category category = new Category(1L, "Концерты");
        event = new Event();
        event.setId(1L);
        event.setState(EventState.PUBLISHED);
        event.setCategory(category);
        event.setInitiator(user);

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Текст");
        comment.setAuthor(user);
        comment.setEvent(event);
        comment.setCreatedOn(LocalDateTime.now());
        comment.setStatus(CommentStatus.PUBLISHED);

        newCommentDto = new NewCommentDto();
        newCommentDto.setText("Новый комментарий");
    }

    @Test
    void createComment_ShouldReturnCommentDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = commentService.createComment(1L, 1L, newCommentDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_WhenEventNotPublished_ShouldThrowConflict() {
        event.setState(EventState.PENDING);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(ConflictException.class,
                () -> commentService.createComment(1L, 1L, newCommentDto));
    }

    @Test
    void updateComment_ShouldUpdateTextAndSetEditedOn() {
        UpdateCommentRequest request = new UpdateCommentRequest();
        request.setText("Обновлённый текст");

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = commentService.updateComment(1L, 1L, request);

        assertEquals("Обновлённый текст", comment.getText());
        assertNotNull(comment.getEditedOn());
        verify(commentRepository).save(comment);
    }

    @Test
    void updateComment_WhenUserNotAuthor_ShouldThrowConflict() {
        UpdateCommentRequest request = new UpdateCommentRequest();
        request.setText("Обновлённый текст");
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThrows(ConflictException.class,
                () -> commentService.updateComment(2L, 1L, request));
    }

    @Test
    void deleteCommentByUser_ShouldDeleteWhenAuthor() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deleteCommentByUser(1L, 1L);

        verify(commentRepository).delete(comment);
    }

    @Test
    void getUserComments_ShouldReturnList() {
        Page<Comment> page = new PageImpl<>(List.of(comment));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findAllByAuthorId(eq(1L), any(PageRequest.class))).thenReturn(page);

        List<CommentDto> result = commentService.getUserComments(1L, 0, 10);

        assertEquals(1, result.size());
        verify(commentRepository).findAllByAuthorId(eq(1L), any());
    }

    @Test
    void getPublishedCommentsByEvent_ShouldReturnOnlyPublished() {
        Page<Comment> page = new PageImpl<>(List.of(comment));
        when(eventRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findAllByEventIdAndStatus(eq(1L), eq(CommentStatus.PUBLISHED), any()))
                .thenReturn(page);

        List<CommentDto> result = commentService.getPublishedCommentsByEvent(1L, 0, 10);

        assertEquals(1, result.size());
    }

    @Test
    void updateCommentStatusByAdmin_ShouldChangeStatus() {
        AdminCommentUpdateRequest request = new AdminCommentUpdateRequest();
        request.setStatus(CommentStatus.BLOCKED);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any())).thenReturn(comment);

        CommentDto result = commentService.updateCommentStatusByAdmin(1L, request);

        assertEquals(CommentStatus.BLOCKED, comment.getStatus());
    }

    @Test
    void deleteCommentByAdmin_ShouldDelete() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deleteCommentByAdmin(1L);

        verify(commentRepository).delete(comment);
    }
}