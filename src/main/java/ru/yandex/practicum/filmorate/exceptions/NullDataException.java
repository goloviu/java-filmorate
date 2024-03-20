package ru.yandex.practicum.filmorate.exceptions;

public class NullDataException extends RuntimeException {

    public NullDataException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public NullDataException(final String message) {
        super(message);
    }
}
