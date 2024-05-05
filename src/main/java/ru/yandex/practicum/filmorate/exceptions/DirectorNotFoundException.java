package ru.yandex.practicum.filmorate.exceptions;

public class DirectorNotFoundException extends RuntimeException {
    public DirectorNotFoundException(final String message) {
        super(message);
    }
}
