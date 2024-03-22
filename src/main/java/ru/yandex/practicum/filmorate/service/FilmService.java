package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exceptions.LikeNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    private static final LocalDate FIRST_FILM_DATE = LocalDate.parse("1895-12-28");
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(InMemoryFilmStorage inMemoryFilmStorage, InMemoryUserStorage inMemoryUserStorage) {
        filmStorage = inMemoryFilmStorage;
        userStorage = inMemoryUserStorage;
    }

    public Film add(final Film film) {
        isValidFilm(film);
        return filmStorage.add(film);
    }

    public Film updateFilm(final Film film) {
        isValidFilm(film);
        return filmStorage.update(film);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public List<Integer> addLike(final Integer filmId, final Integer userId) {
        Film film = filmStorage.getFilmById(filmId);

        if (!userStorage.isUserExist(userId)) {
            throw new UserNotFoundException("Пользователь не найден: " + userId);
        }

        film.getUsersLikes().add(userId);
        return new ArrayList<>(film.getUsersLikes());
    }

    public List<Integer> removeLike(final Integer filmId, final Integer userId) {
        Film film = filmStorage.getFilmById(filmId);

        if (!film.getUsersLikes().contains(userId)) {
            throw new LikeNotFoundException("Лайк пользователя не найден по данному фильму");
        }

        film.getUsersLikes().remove(userId);
        return new ArrayList<>(film.getUsersLikes());
    }

    public List<Film> getPopularFilmsByLikes(final Integer numberOfFilms) {
        if (numberOfFilms <= 0) {
            throw new IncorrectParameterException("Указано неверное количество фильмов, для вывода топа по лайкам: "
                    + numberOfFilms);
        }

        return filmStorage.getAllFilms().stream()
                .sorted((film1, film2) -> (film1.getUsersLikes().size() - film2.getUsersLikes().size()) * -1)
                .limit(numberOfFilms)
                .collect(Collectors.toList());
    }

    public Film getFilmById(final Integer filmId) {
        return filmStorage.getFilmById(filmId);
    }

    public void isValidFilm(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            log.debug("Дата релиза фильма указана раньше 28 Декабря 1895 года: {}", film);
            throw new ValidationException("Дата релиза фильма не может быть до 28 Декабря 1895 года");
        }
    }
}
