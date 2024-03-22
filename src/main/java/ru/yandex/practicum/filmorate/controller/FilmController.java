package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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
        log.info("Получен POST запрос на добавление нового фильма в базу данных: {}", film);
        return filmService.add(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен PUT запрос за обновление информации о фильме. Было:\n{}\n Стало:\n {}",
                filmService.getFilmById(film.getId()), film);
        return filmService.updateFilm(film);
    }

    @GetMapping
    public List<Film> getFilms() {
        log.info("Получен GET запрос на нахождения всех фильмов");
        return filmService.getAllFilms();
    }

    @GetMapping("/{filmId}")
    public Film getFilmById(@PathVariable Integer filmId) {
        log.info("Получен GET запрос на нахождения фильма по ID: {}", filmId);
        return filmService.getFilmById(filmId);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public List<Integer> addLikeToFilm(@PathVariable Integer filmId, @PathVariable Integer userId) {
        log.info("Получен PUT запрос на добавление лайка к фильму по ID: {} Пользователем по ID: {}", filmId, userId);
        return filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public List<Integer> removeLikeFromFilm(@PathVariable Integer filmId, @PathVariable Integer userId) {
        log.info("Получен DELETE запрос на удаление лайка у фильма по ID: {} Пользователем по ID: {}", filmId, userId);
        return filmService.removeLike(filmId, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") Integer count) {
        log.info("Получен GET запрос на получения популярных по лайкам фильмов с ограничением вывода '{}' фильмов",
                count);
        return filmService.getPopularFilmsByLikes(count);
    }
}
