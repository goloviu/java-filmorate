package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NullDataException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(final FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Получен POST запрос на добавление нового фильма в базу данных");
        nullableFilm(film);
        return filmService.add(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен PUT запрос за обновление информации о фильме");
        nullableFilm(film);
        return filmService.updateFilm(film);
    }

    @GetMapping
    public List<Film> getFilms() {
        log.info("Получен GET запрос на нахождения всех фильмов");
        return filmService.getAllFilms();
    }

    @GetMapping("/{filmId}")
    public Film getFilmById(@PathVariable Integer filmId) {
        log.info("Получен GET запрос на нахождения фильма по ID");
        return filmService.getFilmById(filmId);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public List<Integer> addLikeToFilm(@PathVariable Integer filmId, @PathVariable Integer userId) {
        log.info("Получен PUT запрос на добавление лайка к фильму");
        return filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public List<Integer> removeLikeFromFilm(@PathVariable Integer filmId, @PathVariable Integer userId) {
        log.info("Получен DELETE запрос на удаление лайка у фильма");
        return filmService.removeLike(filmId, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") Integer count) {
        log.info("Получен GET запрос на получения популярных по лайкам фильмов");
        return filmService.getPopularFilmsByLikes(count);
    }

    private void nullableFilm(final Film film) {
        if (film == null) {
            throw new NullDataException("Данные о фильме не указаны");
        }
    }
}
