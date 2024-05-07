package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exceptions.LikeException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;
import ru.yandex.practicum.filmorate.model.enums.SearchType;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    private static final LocalDate FIRST_FILM_DATE = LocalDate.parse("1895-12-28");
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private final Comparator<Film> filmLikesComparator = Comparator.comparing(film -> film.getUsersLikes().size());

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film add(final Film film) {
        isValidFilm(film);
        film.setGenres(film.getGenres().stream().sorted(Comparator.comparing(FilmGenre::getId)).collect(Collectors.toCollection(LinkedHashSet::new)));
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
        filmStorage.addUserLikeToFilm(userId, filmId);
        userStorage.saveUserFeed(userId, EventType.LIKE, OperationType.ADD, filmId);
        return new ArrayList<>(film.getUsersLikes());
    }

    public List<Integer> removeLike(final Integer filmId, final Integer userId) {
        Film film = filmStorage.getFilmById(filmId);

        if (!film.getUsersLikes().contains(userId)) {
            throw new LikeException("Лайк пользователя не найден по данному фильму");
        }

        film.getUsersLikes().remove(userId);
        filmStorage.removeUserLike(userId, filmId);
        userStorage.saveUserFeed(userId, EventType.LIKE, OperationType.REMOVE, filmId);
        return new ArrayList<>(film.getUsersLikes());
    }

    public List<Film> getPopularFilms(final Integer numberOfFilms, final Integer genreId, final Integer year) {
        if (numberOfFilms <= 0) {
            throw new IncorrectParameterException("Указано неверное количество фильмов, для вывода топа по лайкам: "
                    + numberOfFilms);
        }
        return filmStorage.getPopularFilms(numberOfFilms, genreId, year);
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
    }

    public List<Film> getDirectorFilms(Integer directorId, String sortBy) {
        log.info("Найденный режисер: {}", filmStorage.getDirectorById(directorId));
        return filmStorage.getDirectorFilms(directorId, sortBy);
    }

    public Director addDirector(Director director) {
        return filmStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        return filmStorage.updateDirector(director);
    }

    public Director deleteDirectorById(Integer directorId) {
        return filmStorage.deleteDirectorById(directorId);
    }

    public List<Director> getAllDirectors() {
        return filmStorage.getAllDirectors();
    }

    public Director getDirectorById(Integer directorId) {
        return filmStorage.getDirectorById(directorId);
    }

    public List<Film> getCommonFriendsFilms(final Integer userId, final Integer friendId) {
        if (!userStorage.isUserExist(userId)) {
            throw new UserNotFoundException("Пользователь не найден: " + userId);
        }
        if (!userStorage.isUserExist(friendId)) {
            throw new UserNotFoundException("Друг не найден: " + friendId);
        }
        Set<Integer> userLikes = userStorage.getUserLikes(userId);
        Set<Integer> friendLikes = userStorage.getUserLikes(friendId);
        HashSet<Integer> likesIntersection = new HashSet<>(userLikes);
        likesIntersection.retainAll(friendLikes);
        if (likesIntersection.isEmpty()) {
            return new ArrayList<>();
        }
        List<Film> commonFilms = filmStorage.getFilmsById(likesIntersection);
        return commonFilms.stream().sorted(filmLikesComparator.reversed()).collect(Collectors.toList());
    }

    public void deleteFilmById(Integer filmId) {
        if (!filmStorage.isFilmExist(filmId)) {
            throw new FilmNotFoundException("Фильм не найден ID: " + filmId);
        }
        filmStorage.remove(filmId);
    }

    public List<Film> searchFilms(String query, Set<SearchType> searchTypes) {
        Set<Film> foundFilms = new HashSet<>();
        searchTypes.forEach(searchType -> {
            switch (searchType) {
                case TITLE: {
                    foundFilms.addAll(filmStorage.searchFilmsByTitleSubstring(query));
                    break;
                }
                case DIRECTOR: {
                    foundFilms.addAll(filmStorage.searchFilmsByDirectorNameSubstring(query));
                    break;
                }
            }
        });
        return foundFilms.stream().sorted(filmLikesComparator.reversed()).collect(Collectors.toList());
    }
}




