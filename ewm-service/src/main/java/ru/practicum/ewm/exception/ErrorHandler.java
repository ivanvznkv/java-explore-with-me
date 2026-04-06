package ru.practicum.ewm.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException e) {
        log.error("404 {}", e.getMessage());
        return buildApiError(HttpStatus.NOT_FOUND, "Запрашиваемый объект не найден.", e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(ConflictException e) {
        log.error("409 {}", e.getMessage());
        return buildApiError(HttpStatus.CONFLICT, "Нарушено ограничение целостности.", e.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(Exception e) {
        log.error("400 {}", e.getMessage());
        return buildApiError(HttpStatus.BAD_REQUEST, "Некорректно составленный запрос.", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleInternalError(Exception e) {
        log.error("500 {}", e.getMessage(), e);
        return buildApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", e.getMessage());
    }

    private ApiError buildApiError(HttpStatus status, String reason, String message) {
        return ApiError.builder()
                .status(status.name())
                .reason(reason)
                .message(message)
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingParams(MissingServletRequestParameterException e) {
        log.error("400 {}", e.getMessage());
        return buildApiError(HttpStatus.BAD_REQUEST, "Отсутствует обязательный параметр", e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolation(ConstraintViolationException e) {
        log.error("400 {}", e.getMessage());
        return buildApiError(HttpStatus.BAD_REQUEST, "Некорректное значение параметра", e.getMessage());
    }

    @ExceptionHandler(ArithmeticException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleArithmetic(ArithmeticException e) {
        log.error("400 {}", e.getMessage());
        return buildApiError(HttpStatus.BAD_REQUEST, "Некорректные параметры пагинации", e.getMessage());
    }
}