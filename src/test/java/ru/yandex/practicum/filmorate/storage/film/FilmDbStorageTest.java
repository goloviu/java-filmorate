package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private FilmDbStorage filmDbStorage;
    private UserDbStorage userDbStorage;

    @BeforeEach
    private void makeNewFilmStorage() {
        this.filmDbStorage = new FilmDbStorage(jdbcTemplate);
        this.userDbStorage = new UserDbStorage(jdbcTemplate);
    }

    private User makeUserWithoutId() {
        return User.builder()
                .name("Test name")
                .login("Test login")
                .email("foo@bar.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .friends(Collections.emptySet())
                .friendsRequests(Collections.emptySet())
                .build();
    }

    private Director makeDirectorWithoutId() {
        return Director.builder()
                .name(" SEaRchEd ")
                .build();
    }

    private void makeEnvironmentForSearchFilm() {
        Film filmTitleSearched = makeFilmWithoutId();
        filmTitleSearched.setName(" SEaRchEd ");
        Film filmDirectorSearched = makeFilmWithoutId();
        List<Director> directors = new ArrayList<>();
        Director director = filmDbStorage.addDirector(makeDirectorWithoutId());
        directors.add(director);
        filmDirectorSearched.setDirectors(directors);
        Film notFoundFilm = makeFilmWithoutId();
        filmDbStorage.add(filmTitleSearched);
        filmDbStorage.add(filmDirectorSearched);
        filmDbStorage.add(notFoundFilm);
    }

    private List<Film> makeEnvironmentForPopularFilmTest() {
        HashSet<FilmGenre> genres = new HashSet<>();
        genres.add(new FilmGenre(1, "Комедия"));

        User user1 = makeUserWithoutId();
        User user2 = makeUserWithoutId();
        user2.setEmail("test@mail.test");
        user2.setLogin("foo");
        User user3 = makeUserWithoutId();
        user3.setEmail("test2@mail.test");
        user3.setLogin("bar");

        userDbStorage.addUser(user1);
        userDbStorage.addUser(user2);
        userDbStorage.addUser(user3);

        Film film1 = makeFilmWithoutId();

        Film film2 = makeFilmWithoutId(); // Фильм с 2я лайками
        film2.setName("Test2");
        film2.setReleaseDate(LocalDate.of(2023, 04, 01));
        film2.setGenres(genres);
        HashSet<Integer> film2Likes = new HashSet<>();
        film2Likes.add(user1.getId());
        film2Likes.add(user2.getId());
        film2.setUsersLikes(film2Likes);

        Film film3 = makeFilmWithoutId();
        film3.setName("Test3");
        film3.setReleaseDate(LocalDate.of(2022, 03, 01));
        film3.setGenres(genres);

        Film film4 = makeFilmWithoutId(); // Фильм с 3я лайками
        film4.setName("Test4");
        film4.setReleaseDate(LocalDate.of(2022, 04, 01));
        film4.setGenres(genres);
        HashSet<Integer> film4Likes = new HashSet<>();
        film4Likes.add(user1.getId());
        film4Likes.add(user2.getId());
        film4Likes.add(user3.getId());
        film4.setUsersLikes(film4Likes);

        Film film5 = makeFilmWithoutId();
        film5.setName("Test5");

        filmDbStorage.add(film1);
        filmDbStorage.add(film2);
        filmDbStorage.add(film3);
        filmDbStorage.add(film4);
        filmDbStorage.add(film5);

        filmDbStorage.addUserLikeToFilm(user1.getId(), film4.getId());
        filmDbStorage.addUserLikeToFilm(user2.getId(), film4.getId());
        filmDbStorage.addUserLikeToFilm(user3.getId(), film4.getId());

        filmDbStorage.addUserLikeToFilm(user1.getId(), film2.getId());
        filmDbStorage.addUserLikeToFilm(user2.getId(), film2.getId());
        return new ArrayList<>(List.of(film1, film2, film3, film4, film5));
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
    void testSearchFilms_ShouldSearch2Films_WhenSearchedByQuerySEaRchEd() {
        //given
        makeEnvironmentForSearchFilm();
        //do
        List<Film> filmsByTitle = filmDbStorage.searchFilmsByTitleSubstring("searched");
        List<Film> filmsByDirector = filmDbStorage.searchFilmsByDirectorNameSubstring("searched");
        //expect
        assertEquals(filmsByTitle.size(), 1);
        assertEquals(filmsByDirector.size(), 1);
        assertNotEquals(filmsByTitle.get(0), filmsByDirector.get(0));
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

    @Test
    void testRemoveFilmById_WhenFilmIsNotNullAndExistInTable() {
        //given
        Film film1 = makeFilmWithoutId();
        Film film2 = makeFilmWithoutId();
        Film film3 = makeFilmWithoutId();
        filmDbStorage.add(film1);
        filmDbStorage.add(film2);
        filmDbStorage.add(film3);
        //do
        filmDbStorage.remove(film3.getId());
        //expect
        String sql = "SELECT COUNT(id) FROM movies";
        Integer columnNum = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(2, columnNum, "Количество записей больше 2х");

        EmptyResultDataAccessException exception = assertThrows(EmptyResultDataAccessException.class,
                () -> filmDbStorage.getFilmById(3), "Исключение не выброшено");
        assertEquals("Incorrect result size: expected 1, actual 0", exception.getMessage());
    }

    @Test
    void testGetPopularFilms_ShouldReturnPopularFilmsByGenreAndYear_WhenFilmsExistsInTable() {
        //given
        List<Film> filmsSavedToDb = makeEnvironmentForPopularFilmTest();
        // do
        List<Film> expectFilms = List.of(filmsSavedToDb.get(3), filmsSavedToDb.get(2));
        List<Film> popularFilmsFromDb = filmDbStorage.getPopularFilms(10, 1, 2022);
        // expect
        assertEquals(expectFilms.size(), popularFilmsFromDb.size(), "Количество популярных фильмов не совпадают");
        assertEquals(expectFilms, popularFilmsFromDb, "Фильмы отсортированы неверно");
        assertThat(expectFilms)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(popularFilmsFromDb);
    }

    @Test
    void testGetPopularFilms_ShouldReturnPopularFilmsByGenre_WhenFilmsExistsInTable() {
        //given
        List<Film> filmsSavedToDb = makeEnvironmentForPopularFilmTest();
        // do
        List<Film> expectFilms = List.of(filmsSavedToDb.get(3), filmsSavedToDb.get(1), filmsSavedToDb.get(2));
        List<Film> popularFilmsFromDb = filmDbStorage.getPopularFilms(10, 1, -1);
        // expect
        assertEquals(expectFilms.size(), popularFilmsFromDb.size(), "Количество популярных фильмов не совпадают");
        assertEquals(expectFilms, popularFilmsFromDb, "Фильмы отсортированы неверно");
        assertThat(expectFilms)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(popularFilmsFromDb);
    }

    @Test
    void testGetPopularFilms_ShouldReturnPopularFilmsByYear_WhenFilmsExistsInTable() {
        //given
        List<Film> filmsSavedToDb = makeEnvironmentForPopularFilmTest();
        // do
        List<Film> expectFilms = List.of(filmsSavedToDb.get(3), filmsSavedToDb.get(2));
        List<Film> popularFilmsFromDb = filmDbStorage.getPopularFilms(10, -1, 2022);
        // expect
        assertEquals(expectFilms.size(), popularFilmsFromDb.size(), "Количество популярных фильмов не совпадают");
        assertEquals(expectFilms, popularFilmsFromDb, "Фильмы отсортированы неверно");
        assertThat(expectFilms)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(popularFilmsFromDb);
    }
}