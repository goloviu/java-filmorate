package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private int filmId = 1;
    private static final HashMap<Integer, Film> films = new HashMap<>();

    @Override
    public Film add(final Film film) {
        checkExistFilmForAddFilm(film);

        film.setId(generateFilmId());
        films.put(film.getId(), film);
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @Override
    public Film remove(final Film film) {
        if (film != null) {
            films.remove(film.getId());
        }
        return film;
    }

    @Override
    public Film update(final Film film) {
        checkExistFilmForUpdateFilm(film);

        Film notUpdatedFilm = films.get(film.getId());
        films.put(film.getId(), film);
        log.info("Информация о фильме обновлена:\nБыло: {} \nСтало: {}\n", notUpdatedFilm, film);
        return film;
    }

    public Film getFilmById(final Integer filmId) {
        if (!films.containsKey(filmId)) {
            throw new FilmNotFoundException("Фильм с не найден: " + filmId);
        }
        return films.get(filmId);
    }

    public List<Film> getAllFilms() {
        log.info("Список всех фильмов передан в сервис");
        return new ArrayList<>(films.values());
    }

    private Integer generateFilmId() {
        return filmId++;
    }

    private void checkExistFilmForAddFilm(final Film film) {
        if (film.getId() != null && films.containsValue(film)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    private void checkExistFilmForUpdateFilm(final Film film) {
        if (film.getId() == null || !films.containsKey(film.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
