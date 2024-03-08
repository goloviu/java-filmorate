package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.FilmorateApplication;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController controller;

    @BeforeEach
    public void newController() {
        controller = new FilmController();
    }

    @Test
    public void tesIsValidFilmShouldThrowValidationExceptionWhenFilmReleaseDateIs1895_12_27() {
        // given
        Film film = new Film(0, "Java Developer", "About strong Java developer",
                LocalDate.parse("1895-12-27"), 60);
        // expect
        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.isValidFilm(film),
                "ValidationException не выбросилось, когда указана дата релиза фильма до 28.12.1895");

        assertEquals("Дата релиза фильма не может быть до 28 Декабря 1895 года", exception.getMessage());
    }

    @Test
    public void testIsValidFilmShouldNotThrowValidationExceptionWhenFilmReleaseDateIs1895_12_28() {
        // given
        Film film = new Film(0, "Java Developer", "About strong Java developer",
                LocalDate.parse("1895-12-28"), 60);
        // expect
        assertDoesNotThrow(() -> controller.isValidFilm(film), "Валидация не должна выбрасывать исключение");
    }
}