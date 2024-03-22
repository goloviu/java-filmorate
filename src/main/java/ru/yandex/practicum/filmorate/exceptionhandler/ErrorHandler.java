package ru.yandex.practicum.filmorate.exceptionhandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exceptions.*;
import ru.yandex.practicum.filmorate.model.ErrorResponse;

@RestControllerAdvice("ru.yandex.practicum.filmorate")
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse incorrectProvidingFilmHandle(final FilmNotFoundException exception) {
        log.warn("Произошла ошибка во время поиска фильма в базе данных.", exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse incorrectParameterHandle(final IncorrectParameterException exception) {
        log.warn("Произошла ошибка при введении параметров пользователем.", exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse likeNotFoundHandle(final LikeNotFoundException exception) {
        log.warn("Произошла ошибка при взаимодействии с лайком пользователя.", exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse userExistHandle(final UserAlreadyExistException exception) {
        log.warn("Произошла ошибка при добавлении нового пользователя.", exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse userNotFoundHandle(final UserNotFoundException exception) {
        log.warn("Произошла ошибка во время поиска пользователя в базе данных.", exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validationHandle(final ValidationException exception) {
        log.warn("Произошла ошибка передачи данных клиента серверу.", exception);
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse unexpectedErrorHandle(final Throwable exception) {
        log.warn("Произошла непредвиденная ошибка.", exception);
        return new ErrorResponse(exception.getMessage());
    }
}
