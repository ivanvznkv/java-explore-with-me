package ru.practicum.ewm.request.dto;

import lombok.Data;

import java.util.Set;

@Data
public class EventRequestStatusUpdateRequest {
    private Set<Long> requestIds;
    private String status;
}