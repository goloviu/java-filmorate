package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

public interface FilmStorage {

    Film add(final Film film);

    Film remove(final Film film);

    Film update(final Film film);
}
