package ru.yandex.practicum.filmorate.exceptions;

public class LikeException extends RuntimeException {

    public LikeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public LikeException(final String message) {
        super(message);
    }
}
