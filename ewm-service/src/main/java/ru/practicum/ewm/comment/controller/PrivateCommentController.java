package ru.practicum.ewm.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CommentRequestDto;
import ru.practicum.ewm.comment.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Validated
public class PrivateCommentController {
    private final CommentService commentService;

    @PostMapping("/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable Long userId,
                                    @Valid @RequestBody CommentRequestDto dto) {
        return commentService.createComment(userId, dto);
    }

    @PatchMapping("/comments")
    public CommentDto updateComment(@PathVariable Long userId,
                                    @Valid @RequestBody CommentRequestDto dto) {
        return commentService.updateComment(userId, dto);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        commentService.deleteCommentByUser(userId, commentId);
    }

    @GetMapping("/comments")
    public List<CommentDto> getUserComments(@PathVariable Long userId,
                                            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                            @RequestParam(defaultValue = "10") @Positive int size) {
        return commentService.getUserComments(userId, from, size);
    }
}