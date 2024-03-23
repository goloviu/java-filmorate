package ru.yandex.practicum.filmorate.exceptions;

public class IncorrectParameterException extends RuntimeException {

    public IncorrectParameterException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public IncorrectParameterException(final String message) {
        super(message);
    }
}
