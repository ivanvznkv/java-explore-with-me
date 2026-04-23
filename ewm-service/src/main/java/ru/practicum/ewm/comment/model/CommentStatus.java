package ru.practicum.ewm.comment.model;

import java.util.Arrays;
import java.util.Optional;

public enum CommentStatus {
    PUBLISHED,
    BLOCKED;

    public static Optional<CommentStatus> from(String value) {
        if (value == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(value))
                .findFirst();
    }
}