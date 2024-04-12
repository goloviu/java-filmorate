package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@Slf4j
public class RatingController {

    private final FilmService filmService;

    @Autowired
    public RatingController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/mpa")
    public List<FilmRating> getAllRatings() {
        log.info("Получен GET запрос на получение всех рейтингов");
        return filmService.getAllRatings();
    }

    @GetMapping("/mpa/{id}")
    public FilmRating getRatingById(@PathVariable("id") final Integer ratingId) {
        log.info("Получен GET запрос на получение рейтинга по ID: {}", ratingId);
        return filmService.getRatingById(ratingId);
    }
}
