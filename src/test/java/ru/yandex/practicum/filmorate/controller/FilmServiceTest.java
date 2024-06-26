package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmServiceTest {

    private final JdbcTemplate jdbcTemplate;
    private FilmService filmService;

    @BeforeEach
    public void newController() {
        filmService = new FilmService(new FilmDbStorage(jdbcTemplate), new UserDbStorage(jdbcTemplate));
    }

    @Test
    public void tesIsValidFilmShouldThrowValidationExceptionWhenFilmReleaseDateIs1895_12_27() {
        // given
        Film film = new Film(0, "Java Developer", "About strong Java developer",
                LocalDate.parse("1895-12-27"), 60, new FilmRating(1), Collections.emptySet(), Collections.emptySet());
        // expect
        ValidationException exception = assertThrows(ValidationException.class, () -> filmService.isValidFilm(film),
                "ValidationException не выбросилось, когда указана дата релиза фильма до 28.12.1895");

        assertEquals("Дата релиза фильма не может быть до 28 Декабря 1895 года", exception.getMessage());
    }

    @Test
    public void testIsValidFilmShouldNotThrowValidationExceptionWhenFilmReleaseDateIs1895_12_28() {
        // given
        HashSet<FilmGenre> testGenres = new HashSet<>();
        testGenres.add(new FilmGenre(1));
        Film film = new Film(0, "Java Developer", "About strong Java developer",
                LocalDate.parse("1895-12-28"), 60,new FilmRating(1), testGenres, Collections.emptySet());
        // expect
        assertDoesNotThrow(() -> filmService.isValidFilm(film), "Валидация не должна выбрасывать исключение");
    }
}