package ru.practicum.ewm.comment.service;

import ru.practicum.ewm.comment.dto.*;

import java.util.List;

public interface CommentService {
    CommentDto createComment(Long userId, CommentRequestDto dto);

    CommentDto updateComment(Long userId, CommentRequestDto dto);

    void deleteCommentByUser(Long userId, Long commentId);

    List<CommentDto> getUserComments(Long userId, int from, int size);

    List<CommentDto> getPublishedCommentsByEvent(Long eventId, int from, int size);

    List<CommentDto> searchCommentsAdmin(Long eventId, Long authorId, String status, int from, int size);

    CommentDto updateCommentStatusByAdmin(Long commentId, AdminCommentUpdateRequest request);

    void deleteCommentByAdmin(Long commentId);
}