package ru.yandex.practicum.filmorate.exceptions;

public class ValidationException extends RuntimeException {

    public ValidationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ValidationException(final String message) {
        super(message);
    }
}
