package ru.yandex.practicum.filmorate.exceptions;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundException(final String message) {
        super(message);
    }
}
