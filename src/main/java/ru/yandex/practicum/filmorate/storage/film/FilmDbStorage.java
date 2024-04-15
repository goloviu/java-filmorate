package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmRating;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component("filmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film add(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("movies")
                .usingGeneratedKeyColumns("id");

        Integer filmIdFromDbAfterInsert = simpleJdbcInsert.executeAndReturnKey(film.toMap()).intValue();
        film.setId(filmIdFromDbAfterInsert);

        addGenresToDb(film);

        log.info("Фильм добавлен в базу данных в таблицу movies по ID: {} \n {}", filmIdFromDbAfterInsert, film);
        return film;
    }

    @Override
    public Film remove(Film film) {
        String sqlDeleteFilm = "DELETE FROM movies WHERE id = ?";

        jdbcTemplate.update(sqlDeleteFilm, film.getId());
        log.info("Фильм по ID: {} и лайки пользователей удалены из базы данных в таблицах movies, movie_like.",
                film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE movies SET title = ?, description = ?, duration = ?, release_date = ?, rating_id = ? " +
                "WHERE id = ?";

        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getDuration(), film.getReleaseDate(),
                film.getMpa().getId(), film.getId());

        addGenresToDb(film);

        String sqlReadQuery = "SELECT * FROM movies WHERE id = ?";
        Film updatedFilm = jdbcTemplate.queryForObject(sqlReadQuery, this::mapRowToFilm, film.getId());
        log.info("Фильм успешно обновлен в базе данных по таблице movies. \n {}", updatedFilm);
        return updatedFilm;
    }

    @Override
    public Film getFilmById(Integer filmId) {
        String sqlReadQuery = "SELECT * FROM movies WHERE id = ?";
        Film film = jdbcTemplate.queryForObject(sqlReadQuery, this::mapRowToFilm, filmId);
        log.info("Получен фильм из базы данных по таблице movies. \n {}", film);
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        String sqlReadQuery = "SELECT * FROM movies";
        List<Film> films = jdbcTemplate.query(sqlReadQuery, this::mapRowToFilm);
        log.info("Получен список фильмов из базы данных по таблице movies. \n {}", films);
        return films;
    }

    @Override
    public List<FilmGenre> getAllGenres() {
        String sql = "SELECT * FROM genre ORDER BY ID";
        List<FilmGenre> genres = jdbcTemplate.query(sql, this::mapRowToGenre);
        log.info("Из базы данных получен список всех жанров по таблице genre. \n {}", genres);
        return genres;
    }

    @Override
    public FilmGenre getGenreById(final Integer genreId) {
        String sql = "SELECT * FROM genre WHERE id = ?";
        FilmGenre filmGenre = jdbcTemplate.queryForObject(sql, this::mapRowToGenre, genreId);
        log.info("Из базы данных получен жанр по ID: {} в таблице genre. \n {}", genreId, filmGenre);
        return filmGenre;
    }

    @Override
    public List<FilmRating> getAllRatings() {
        String sql = "SELECT * FROM rating ORDER BY ID";
        List<FilmRating> filmRatings = jdbcTemplate.query(sql, this::mapRowToRating);
        log.info("Из базы данных получен список рейтингов фильмов. \n {}", filmRatings);
        return filmRatings;
    }

    @Override
    public FilmRating getRatingById(Integer ratingId) {
        String sql = "SELECT * FROM rating WHERE id = ?";
        FilmRating filmRating = jdbcTemplate.queryForObject(sql, this::mapRowToRating, ratingId);
        log.info("Из базы данных получен рейтинг по ID: {} в таблице rating. \n {}", ratingId, filmRating);
        return filmRating;
    }

    @Override
    public boolean addUserLikeToFilm(final Integer userId, final Integer filmId) {
        String sql = "INSERT INTO movie_like (movie_id, user_id) " +
                "VALUES(?, ?)";

        boolean isAddedLikeToFilm = jdbcTemplate.update(sql, filmId, userId) > 1;
        if (isAddedLikeToFilm) {
            log.info("Пользователь ID {} поставил лайк фильму ID {}", userId, filmId);
        }
        return isAddedLikeToFilm;
    }

    @Override
    public boolean removeUserLike(final Integer userId, final Integer filmId) {
        String sql = "DELETE FROM movie_like WHERE movie_id = ? AND user_id = ?";

        boolean isLikeRemoved = jdbcTemplate.update(sql, filmId, userId) > 1;
        if (isLikeRemoved) {
            log.info("Пользователь ID {} удалил лайк у фильма ID {}", userId, filmId);
        }
        return isLikeRemoved;
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Integer filmId = rs.getInt("id");

        return Film.builder()
                .id(filmId)
                .name(rs.getString("title"))
                .description(rs.getString("description"))
                .duration(rs.getInt("duration"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .mpa(getRatingById(rs.getInt("rating_id")))
                .genres(getGenresFromDb(filmId))
                .usersLikes(getUsersLikedFilmFromDb(filmId))
                .build();
    }

    private FilmGenre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return FilmGenre.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .build();
    }

    private FilmRating mapRowToRating(ResultSet rs, int rowNum) throws SQLException {
        return FilmRating.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .build();
    }

    private Set<Integer> getUsersLikedFilmFromDb(final Integer filmId) {
        String sql = "SELECT * FROM movie_like WHERE movie_id = ?";

        Set<Integer> filmLikedUsersIds = new HashSet<>();

        jdbcTemplate.query(sql, rs -> {
            filmLikedUsersIds.add(rs.getInt("user_id"));
        }, filmId);
        return filmLikedUsersIds;
    }

    private void addGenresToDb(final Film film) {
        String sql = "INSERT INTO movie_genre (movie_id, genre_id) VALUES (?, ?)";
        List<FilmGenre> genres = new ArrayList<>(film.getGenres());
        Integer filmId = film.getId();

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                FilmGenre genre = genres.get(i);
                ps.setInt(1, filmId);
                ps.setInt(2, genre.getId());
            }

            @Override
            public int getBatchSize() {
                return genres.size();
            }
        });
    }

    private Set<FilmGenre> getGenresFromDb(final Integer filmId) {
        String sql = "SELECT g.id, g.name FROM movie_genre mg " +
                "JOIN genre g ON mg.genre_id = g.id " +
                "WHERE mg.movie_id = ? " +
                "ORDER BY g.id";

        return new HashSet<>(jdbcTemplate.query(sql, this::mapRowToGenre, filmId));
    }
}
