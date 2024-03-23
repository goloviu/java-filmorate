package ru.yandex.practicum.filmorate.exceptions;

public class FilmNotFoundException extends RuntimeException {

    public FilmNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public FilmNotFoundException(final String message) {
        super(message);
    }
}
