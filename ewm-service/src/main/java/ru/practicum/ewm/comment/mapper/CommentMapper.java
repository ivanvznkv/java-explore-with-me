package ru.practicum.ewm.comment.mapper;

import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.model.CommentStatus;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;

public class CommentMapper {

    public static Comment toComment(String text, User author, Event event) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setCreatedOn(LocalDateTime.now());
        comment.setStatus(CommentStatus.PUBLISHED);
        return comment;
    }

    public static CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                UserMapper.toUserShortDto(comment.getAuthor()),
                comment.getEvent().getId(),
                comment.getCreatedOn(),
                comment.getEditedOn(),
                comment.getStatus().name()
        );
    }
}