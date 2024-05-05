package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/directors")
@Slf4j
public class DirectorController {
    private final FilmService filmService;

    @Autowired
    public DirectorController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Director addDirector(@Valid @RequestBody Director director) {
        log.info("Получен POST запрос на добавление нового режисера в базу данных: {}", director);
        return filmService.addDirector(director);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        log.info("Получен PUT запрос на обновление информации о режисере. Было:\n{}\n Стало:\n {}",
                filmService.getDirectorById(director.getId()), director);
        return filmService.updateDirector(director);
    }

    @DeleteMapping("/{directorId}")
    public Director deleteDirectorById(@PathVariable Integer directorId) {
        log.info("Получен DELETE запрос на удаление режисера по ID: {}", directorId);
        return filmService.deleteDirectorById(directorId);
    }

    @GetMapping
    public List<Director> getAllDirectors() {
        log.info("Получен GET запрос на нахождения всех режисеров");
        return filmService.getAllDirectors();
    }

    @GetMapping("/{directorId}")
    public Director getDirectorById(@PathVariable Integer directorId) {
        log.info("Получен GET запрос на нахождения режисера по ID: {}", directorId);
        return filmService.getDirectorById(directorId);
    }
}
