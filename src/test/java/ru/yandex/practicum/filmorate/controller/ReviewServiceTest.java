package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReviewServiceTest {

    private final JdbcTemplate jdbcTemplate;
    private ReviewService reviewService;
    private UserDbStorage userDbStorage;
    private FilmDbStorage filmDbStorage;

    @BeforeEach
    public void newController() {
        userDbStorage = new UserDbStorage(jdbcTemplate);
        filmDbStorage = new FilmDbStorage(jdbcTemplate);
        reviewService = new ReviewService(new ReviewDbStorage(jdbcTemplate), userDbStorage, filmDbStorage);
    }

    @Test
    public void testCheckValidThrowsValidExUserNonexistent() {
        HashSet<FilmGenre> testGenres = new HashSet<>();
        testGenres.add(new FilmGenre(1));
        Film film = new Film(0, "Java Developer", "About strong Java developer",
                LocalDate.parse("1895-12-28"), 60, new FilmRating(1), testGenres,
                Collections.emptySet(), Collections.emptyList());

        Review review = new Review(0, "Content", true, 666, filmDbStorage.add(film).getId(), 0);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> reviewService.checkValid(review),
                "UserNotFoundException не выбросилось, когда указан несуществующий юзер");

        assertEquals("Пользователь не найден: 666", exception.getMessage());
    }

    @Test
    public void testCheckValidThrowsValidExFilmNonexistent() {
        User user = new User(0, "user0@mail.ru", "user0", "userName",
                LocalDate.parse("2000-12-27"), new HashSet<>(), new HashSet<>());

        Review review = new Review(0, "Content", true, userDbStorage.addUser(user).getId(),
                666, 0);

        FilmNotFoundException exception = assertThrows(FilmNotFoundException.class, () -> reviewService.checkValid(review),
                "FilmNotFoundException не выбросилось, когда указан несуществующий фильм");

        assertEquals("Фильм не найден: 666", exception.getMessage());
    }

    @Test
    public void testCheckValidNotThrowsValidEx() {
        User user = new User(0, "user0@mail.ru", "user0", "userName",
                LocalDate.parse("2000-12-27"), new HashSet<>(), new HashSet<>());
        HashSet<FilmGenre> testGenres = new HashSet<>();
        testGenres.add(new FilmGenre(1));
        Film film = new Film(0, "Java Developer", "About strong Java developer",
                LocalDate.parse("1895-12-28"), 60, new FilmRating(1), testGenres,
                Collections.emptySet(), Collections.emptyList());

        Review review = new Review(0, "Content", true, userDbStorage.addUser(user).getId(),
                filmDbStorage.add(film).getId(), 0);

        assertDoesNotThrow(() -> reviewService.checkValid(review), "Валидация не должна выбрасывать исключение");
    }
}
