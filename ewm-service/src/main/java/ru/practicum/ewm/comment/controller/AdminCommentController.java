package ru.practicum.ewm.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.AdminCommentUpdateRequest;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Validated
public class AdminCommentController {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> searchComments(@RequestParam(required = false) Long eventId,
                                           @RequestParam(required = false) Long authorId,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                           @RequestParam(defaultValue = "10") @Positive int size) {
        return commentService.searchCommentsAdmin(eventId, authorId, status, from, size);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateCommentStatus(@PathVariable Long commentId,
                                          @Valid @RequestBody AdminCommentUpdateRequest request) {
        return commentService.updateCommentStatusByAdmin(commentId, request);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        commentService.deleteCommentByAdmin(commentId);
    }
}