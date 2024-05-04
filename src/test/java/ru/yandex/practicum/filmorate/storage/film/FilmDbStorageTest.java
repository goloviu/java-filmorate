package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmRating;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private FilmDbStorage filmDbStorage;

    @BeforeEach
    private void makeNewFilmStorage() {
        this.filmDbStorage = new FilmDbStorage(jdbcTemplate);
    }

    private Film makeFilmWithoutId() {
        return Film.builder()
                .name("Java and a bit more about coffee")
                .description("about coffee")
                .releaseDate(LocalDate.of(2024, 04, 11))
                .duration(200)
                .mpa(new FilmRating(1, "G"))
                .genres(Collections.emptySet())
                .usersLikes(Collections.emptySet())
                .directors(Collections.emptyList())
                .build();
    }

    private List<FilmRating> getRatingList() {
        return List.of(new FilmRating(1, "G"),
                new FilmRating(2, "PG"),
                new FilmRating(3, "PG-13"),
                new FilmRating(4, "R"),
                new FilmRating(5, "NC-17"));
    }

    private List<FilmGenre> getGenresList() {
        return List.of(new FilmGenre(1, "Комедия"),
                new FilmGenre(2, "Драма"),
                new FilmGenre(3, "Мультфильм"),
                new FilmGenre(4, "Триллер"),
                new FilmGenre(5, "Документальный"),
                new FilmGenre(6, "Боевик"));
    }

    @Test
    void testAdd_ShouldSaveFilmToDb_WhenFilmIsNotNull() {
        //given
        Film film = makeFilmWithoutId();
        //do
        Integer filmId = filmDbStorage.add(film).getId();
        Film savedFilm = filmDbStorage.getFilmById(filmId);
        //expect
        assertThat(savedFilm)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(film);
    }

    @Test
    void testRemove_ShouldDeleteFilmId3_WhenFilmIsNotNullAndExistInTable() {
        //given
        Film film1 = makeFilmWithoutId();
        Film film2 = makeFilmWithoutId();
        Film film3 = makeFilmWithoutId();
        filmDbStorage.add(film1);
        filmDbStorage.add(film2);
        filmDbStorage.add(film3);
        //do
        Integer deletedFilmId = filmDbStorage.remove(film3).getId();
        //expect
        String sql = "SELECT COUNT(id) FROM movies";
        Integer columnNum = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(2, columnNum, "Количество записей больше 2х");

        EmptyResultDataAccessException exception = assertThrows(EmptyResultDataAccessException.class,
                () -> filmDbStorage.getFilmById(deletedFilmId), "Исключение не выброшено");
        assertEquals("Incorrect result size: expected 1, actual 0", exception.getMessage());
    }

    @Test
    void testUpdate_ShouldChangeFilmNameAndDescriptionAndDurationBySameId_WhenFilmIsNotNullAndExistInTable() {
        //given
        Film film = makeFilmWithoutId();
        Integer filmId = filmDbStorage.add(film).getId();

        Film filmForUpdate = makeFilmWithoutId();
        filmForUpdate.setId(filmId);
        filmForUpdate.setName("Java island");
        filmForUpdate.setDescription("coffee place");
        filmForUpdate.setDuration(200);
        //do
        filmDbStorage.update(filmForUpdate);
        Film updatedFilm = filmDbStorage.getFilmById(filmId);
        //expect
        assertThat(updatedFilm)
                .isNotNull()
                .usingRecursiveComparison()
                .isNotEqualTo(film);
        assertEquals("Java island", updatedFilm.getName(), "Названия не совпадают");
        assertEquals("coffee place", updatedFilm.getDescription(), "Описания не совпадают");
        assertEquals(200, updatedFilm.getDuration(), "Продолжительность не совпадает");
    }

    @Test
    void testGetFilmById_ShouldReturnSavedFilm_WhenFilmIsNotNull() {
        //given
        Film film = makeFilmWithoutId();
        Integer filmId = filmDbStorage.add(film).getId();
        //do
        Film savedFilm = filmDbStorage.getFilmById(filmId);
        //expect
        assertThat(savedFilm)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(film);
    }

    @Test
    void testGetFilmsById_ShouldReturnSavedFilms_WhenFilmIsNotNull() {
        //given
        Film film1 = makeFilmWithoutId();
        Film film2 = makeFilmWithoutId();
        Integer filmId1 = filmDbStorage.add(film1).getId();
        Integer filmId2 = filmDbStorage.add(film2).getId();
        //do
        Set<Integer> filmsId = new HashSet<>();
        filmsId.add(filmId1);
        filmsId.add(filmId2);
        List<Film> savedFilms = filmDbStorage.getFilmsById(filmsId);
        //expect
        assertEquals(2, savedFilms.size(), "Размер списка фильмов не совпадает");
        assertThat(savedFilms)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(savedFilms);
    }

    @Test
    void testGetAllFilms_ShouldReturnListOf3Films_WhenFilmsAreNotNull() {
        //given
        Film film1 = makeFilmWithoutId();
        Film film2 = makeFilmWithoutId();
        film2.setName("Test2");
        Film film3 = makeFilmWithoutId();
        film3.setName("Test3");
        filmDbStorage.add(film1);
        filmDbStorage.add(film2);
        filmDbStorage.add(film3);
        List<Film> films = new ArrayList<>();
        films.add(film1);
        films.add(film2);
        films.add(film3);
        //do
        List<Film> savedFilms = filmDbStorage.getAllFilms();
        //expect
        assertEquals(3, films.size(), "Размер списка фильмов не совпадает");
        assertThat(savedFilms)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(savedFilms);
    }

    @Test
    void testGetAllGenres_ShouldReturnListOfGenres_WhenDbTableGenreIsNotEmpty() {
        //given
        List<FilmGenre> genres = getGenresList();
        //do
        List<FilmGenre> savedGenres = filmDbStorage.getAllGenres();
        //expect
        assertThat(savedGenres)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(genres);
    }

    @Test
    void testGetGenreById_ShouldCompareEveryGenreWithGenreInDb_WhenDbTableGenreIsNotEmpty() {
        //given
        List<FilmGenre> genres = getGenresList();
        //expect
        for (FilmGenre genre : genres) {
            FilmGenre filmGenre = filmDbStorage.getGenreById(genre.getId());
            assertEquals(genre.getName(), filmGenre.getName());
        }
    }

    @Test
    void testGetAllRatings_ShouldReturnListOfAllRatings_WhenDbTableRatingIsNotEmpty() {
        //given
        List<FilmRating> ratings = getRatingList();
        //do
        List<FilmRating> savedRatings = filmDbStorage.getAllRatings();
        //expect
        assertThat(savedRatings)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(ratings);
    }

    @Test
    void testGetRatingId_ShouldReturnRandomRating_WhenDbTableRatingIsNotEmpty() {
        //given
        List<FilmRating> ratings = getRatingList();
        //expect
        for (FilmRating rating : ratings) {
            FilmRating filmRating = filmDbStorage.getRatingById(rating.getId());
            assertEquals(rating.getName(), filmRating.getName());
        }
    }
}