package ru.yandex.practicum.filmorate.exceptions;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(final String message) {
        super(message);
    }
}