package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.event.util.SortType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PublicEventSearchParams {
    private String text;
    private List<Long> categories;
    private Boolean paid;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

    @Builder.Default
    private Boolean onlyAvailable = false;

    private SortType sort;

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