package ru.yandex.practicum.filmorate.exceptions;

public class LikeNotFoundException extends RuntimeException {

    public LikeNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public LikeNotFoundException(final String message) {
        super(message);
    }
}
