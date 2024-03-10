package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private static final LocalDate FIRST_FILM_DATE = LocalDate.parse("1895-12-28");
    private int filmId = 1;
    private HashMap<Integer, Film> films = new HashMap<>();

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        if (film.getId() != null && films.containsValue(film)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        isValidFilm(film);
        film.setId(generateFilmId());
        films.put(film.getId(), film);
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        if (film.getId() == null || !films.containsKey(film.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        isValidFilm(film);
        Film notUpdatedFilm = films.get(film.getId());
        films.put(film.getId(), film);
        log.info("Информация о фильме обновлена:\nБыло: {} \nСтало: {}\n", notUpdatedFilm, film);
        return film;
    }

    @GetMapping
    public List<Film> getFilms() {
        log.info("Получен GET запрос на нахождения всех фильмов");
        return new ArrayList<>(films.values());
    }

    private Integer generateFilmId() {
        return filmId++;
    }

    protected void isValidFilm(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            log.debug("Дата релиза фильма указана раньше 28 Декабря 1895 года: {}", film);
            throw new ValidationException("Дата релиза фильма не может быть до 28 Декабря 1895 года");
        }
    }
}
