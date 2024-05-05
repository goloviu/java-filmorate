package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmRating;

import java.util.List;
import java.util.Set;

public interface FilmStorage {

    Film add(final Film film);

    Film remove(final Film film);

    void remove(Integer filmId);

    Film update(final Film film);

    Film getFilmById(final Integer filmId);

    List<Film> getFilmsById(Set<Integer> filmIds);

    List<Film> getAllFilms();

    List<FilmGenre> getAllGenres();

    FilmGenre getGenreById(final Integer genreId);

    List<FilmRating> getAllRatings();

    FilmRating getRatingById(final Integer ratingId);

    boolean addUserLikeToFilm(final Integer userId, final Integer filmId);

    boolean removeUserLike(final Integer userId, final Integer filmId);

    boolean isFilmExist(Integer filmId);

    List<Film> getDirectorFilms(Integer directorId, String sortBy);

    Director addDirector(Director director);

    Director updateDirector(Director director);

    Director deleteDirectorById(Integer directorId);

    List<Director> getAllDirectors();

    Director getDirectorById(Integer directorId);
}




