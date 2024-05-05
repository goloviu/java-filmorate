package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exceptions.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DirectorServiceTest {
    private final JdbcTemplate jdbcTemplate;
    private FilmService filmService;

    @BeforeEach
    public void newController() {
        filmService = new FilmService(new FilmDbStorage(jdbcTemplate), new UserDbStorage(jdbcTemplate));
    }

    @Test
    public void testAddingDirectorToTheDbAndCheckingObjectsForEquality() {
        Director director = Director.builder()
                .id(1)
                .name("new director")
                .build();

        Director savedDirector = filmService.addDirector(director);

        assertEquals(director, savedDirector, "Разные режисеры!");
    }

    @Test
    public void testUpdatedDirectorToTheDbAndCheckingObjectsForEquality() {
        Director director = Director.builder()
                .id(1)
                .name("new director")
                .build();

        Director savedDirector = filmService.addDirector(director);

        Director currentDirector = Director.builder()
                .id(savedDirector.getId())
                .name("new name director")
                .build();

        Director updatedDirector = filmService.updateDirector(currentDirector);

        assertEquals(updatedDirector, currentDirector, "Разные режисеры!");
    }

    @Test
    public void testGetAllDirectorsTheLengthOfTheListsAndTheObjectsMustBeEqual() {
        Director director1 = Director.builder()
                .id(1)
                .name("new director1")
                .build();

        Director director2 = Director.builder()
                .id(2)
                .name("new director2")
                .build();

        Director savedDirector1 = filmService.addDirector(director1);
        Director savedDirector2 = filmService.addDirector(director2);

        List<Director> directors = filmService.getAllDirectors();

        assertEquals(2, directors.size(), "Разная длина списка!");
        assertEquals(savedDirector1, directors.get(0));
        assertEquals(savedDirector2, directors.get(1));
    }

    @Test
    public void testGetDirectorByIdTheFoundDirectorMustBeEqualToTheAddedOne() {
        Director director = Director.builder()
                .id(1)
                .name("new director")
                .build();

        filmService.addDirector(director);

        Director foundDirector = filmService.getDirectorById(director.getId());

        assertEquals(foundDirector, director);
    }

    @Test
    public void testDeleteDirectorByIdExceptionShouldBeThrownWhenSearchingForADeletedDirector() {
        Director director = Director.builder()
                .id(1)
                .name("new director")
                .build();

        filmService.addDirector(director);
        filmService.deleteDirectorById(director.getId());

        DirectorNotFoundException exception = assertThrows(DirectorNotFoundException.class,
                () -> filmService.getDirectorById(director.getId()),
                "DirectorNotFoundException не выбросилось, когда указан несуществующий режисер");

        assertEquals("Режисер с id: " + director.getId() + " не найден", exception.getMessage());
    }
}
