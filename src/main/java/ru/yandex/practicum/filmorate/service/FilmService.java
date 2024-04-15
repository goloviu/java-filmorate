package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exceptions.LikeException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    private static final LocalDate FIRST_FILM_DATE = LocalDate.parse("1895-12-28");
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
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

        if (film.getUsersLikes().contains(userId)) {
            throw new LikeException("Пользователь с ID:" + userId + "уже поставил лайк этому фильму");
        }

        film.getUsersLikes().add(userId);
        filmStorage.addUserLikeToFilm(userId, filmId);
        return new ArrayList<>(film.getUsersLikes());
    }

    public List<Integer> removeLike(final Integer filmId, final Integer userId) {
        Film film = filmStorage.getFilmById(filmId);

        if (!film.getUsersLikes().contains(userId)) {
            throw new LikeException("Лайк пользователя не найден по данному фильму");
        }

        film.getUsersLikes().remove(userId);
        filmStorage.removeUserLike(userId, filmId);
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

    public List<FilmGenre> getAllGenres() {
        return filmStorage.getAllGenres();
    }

    public FilmGenre getGenreById(final Integer genreId) {
        return filmStorage.getGenreById(genreId);
    }

    public List<FilmRating> getAllRatings() {
        return filmStorage.getAllRatings();
    }

    public FilmRating getRatingById(final Integer ratingId) {
        return filmStorage.getRatingById(ratingId);
    }

    public void isValidFilm(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            log.debug("Дата релиза фильма указана раньше 28 Декабря 1895 года: {}", film);
            throw new ValidationException("Дата релиза фильма не может быть до 28 Декабря 1895 года");
        }

        Integer ratingId = film.getMpa().getId();
        try {
            filmStorage.getRatingById(ratingId);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Неверно указан рейтинг фильма: {}", ratingId);
            throw new ValidationException("Ошибка валидации, неверно указан рейтинг(mpa) фильма: " + ratingId);
        }

        if (!film.getGenres().isEmpty()) {
            for (FilmGenre genre : film.getGenres()) {
                try {
                    filmStorage.getGenreById(genre.getId());
                } catch (EmptyResultDataAccessException e) {
                    log.debug("Неверно указан жанр фильма: {}", genre.getId());
                    throw new ValidationException("Ошибка валидации, неверно указан жанр фильма: " + genre.getId());
                }
            }
        }
    }
}
