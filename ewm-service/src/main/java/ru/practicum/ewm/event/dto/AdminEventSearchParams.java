package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminEventSearchParams {
    private List<Long> users;
    private List<String> states;
    private List<Long> categories;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

    @PositiveOrZero
    @Builder.Default
    private int from = 0;

    @Positive
    @Builder.Default
    private int size = 10;

    @AssertTrue(message = "Дата начала не может быть позже даты окончания")
    public boolean isRangeStartBeforeRangeEnd() {
        if (rangeStart == null || rangeEnd == null) return true;
        return !rangeStart.isAfter(rangeEnd);
    }
}