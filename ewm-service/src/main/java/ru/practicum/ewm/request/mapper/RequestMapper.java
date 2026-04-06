package ru.practicum.ewm.request.mapper;

import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class RequestMapper {

    public static Request toRequest(Event event, User requester) {
        Request request = new Request();
        request.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
        request.setEvent(event);
        request.setRequester(requester);
        request.setStatus(RequestStatus.PENDING);
        return request;
    }

    public static ParticipationRequestDto toDto(Request request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getCreated(),
                request.getEvent().getId(),
                request.getRequester().getId(),
                request.getStatus()
        );
    }
}