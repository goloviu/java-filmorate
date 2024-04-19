package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@Slf4j
public class GenreController {

    private final FilmService filmService;

    @Autowired
    public GenreController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/genres")
    public List<FilmGenre> getAllGenres() {
        log.info("Получен GET запрос на нахождение всех жанров");
        return filmService.getAllGenres();
    }

    @GetMapping("/genres/{id}")
    public FilmGenre getGenreById(@PathVariable("id") final Integer genreId) {
        log.info("Получен GET запрос на получение жанра по ID: {}", genreId);
        return filmService.getGenreById(genreId);
    }
}
