package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {

    private FilmService filmService;

    @BeforeEach
    public void newController() {
        filmService = new FilmService(new InMemoryFilmStorage(), new InMemoryUserStorage());
    }

    @Test
    public void tesIsValidFilmShouldThrowValidationExceptionWhenFilmReleaseDateIs1895_12_27() {
        // given
        Film film = new Film(0, "Java Developer", "About strong Java developer",
                LocalDate.parse("1895-12-27"), 60, Collections.emptySet());
        // expect
        ValidationException exception = assertThrows(ValidationException.class, () -> filmService.isValidFilm(film),
                "ValidationException не выбросилось, когда указана дата релиза фильма до 28.12.1895");

        assertEquals("Дата релиза фильма не может быть до 28 Декабря 1895 года", exception.getMessage());
    }

    @Test
    public void testIsValidFilmShouldNotThrowValidationExceptionWhenFilmReleaseDateIs1895_12_28() {
        // given
        Film film = new Film(0, "Java Developer", "About strong Java developer",
                LocalDate.parse("1895-12-28"), 60, Collections.emptySet());
        // expect
        assertDoesNotThrow(() -> filmService.isValidFilm(film), "Валидация не должна выбрасывать исключение");
    }
}