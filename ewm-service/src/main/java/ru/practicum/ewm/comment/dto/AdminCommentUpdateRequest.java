package ru.practicum.ewm.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.ewm.comment.model.CommentStatus;

@Data
public class AdminCommentUpdateRequest {
    @NotNull(message = "Статус обязателен")
    private CommentStatus status;
}