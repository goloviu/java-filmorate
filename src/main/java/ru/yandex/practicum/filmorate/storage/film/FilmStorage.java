package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film add(final Film film);

    Film remove(final Film film);

    Film update(final Film film);

    Film getFilmById(final Integer filmId);

    List<Film> getAllFilms();
}
