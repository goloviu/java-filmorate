package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    public List<Film> getPopularFilmsByLikes(@RequestParam(defaultValue = "10") Integer count,
                                             @RequestParam(defaultValue = "-1") Integer genreId,
                                             @RequestParam(defaultValue = "-1") Integer year) {
        log.info("Получен GET запрос на получения популярных по лайкам фильмов с ограничением вывода '{}' фильмов, " +
                        "жанр ID {}, год {}", count, genreId, year);
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getDirectorFilms(@PathVariable Integer directorId,
                                       @RequestParam(defaultValue = "year") String sortBy) {
        log.info("Получен GET запрос на нахождения фильмов по ID режиссера: {}", directorId);
        return filmService.getDirectorFilms(directorId, sortBy);
    }

    @GetMapping("/common")
    public List<Film> getCommonFriendsFilms(@RequestParam Integer userId, @RequestParam Integer friendId) {
        log.info("Получен GET запрос на вывод общих с другом фильмов с сортировкой по их популярности (Пользователь ID: {}, друг ID: {})",
                userId, friendId);
        return filmService.getCommonFriendsFilms(userId, friendId);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilmById(@PathVariable Integer filmId) {
        log.info("Получен DELETE запрос на удаление фильма по ID: {}", filmId);
        filmService.deleteFilmById(filmId);
    }

}
