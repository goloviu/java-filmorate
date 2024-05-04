package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmRating;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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
        addDirectorsToDb(film);

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
        String sqlDeleteGenres = "DELETE FROM movie_genre WHERE movie_id = ?";
        jdbcTemplate.update(sqlDeleteGenres, film.getId());

        String sql = "UPDATE movies SET title = ?, description = ?, duration = ?, release_date = ?, rating_id = ? " +
                "WHERE id = ?";

        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getDuration(), film.getReleaseDate(),
                film.getMpa().getId(), film.getId());

        addGenresToDb(film);
        addDirectorsToDb(film);

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
    public List<Film> getFilmsById(Set<Integer> filmIds) {
        String sqlParametersPart = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sqlReadQuery = String.format("SELECT * FROM movies WHERE id IN (%s)", sqlParametersPart);
        List<Film> films = jdbcTemplate.query(sqlReadQuery, filmIds.toArray(), this::mapRowToFilm);
        log.info("Получен список фильмов по id's из базы данных по таблице movies. \n {}", films);
        return films;
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

    @Override
    public List<Film> getDirectorFilms(Integer directorId, String sortBy) {
        String sqlReadQuery = "SELECT m.id, m.title, m.description, m.duration, m.release_date, m.rating_id " +
                "FROM movies AS m " +
                "JOIN director_movies AS dm " +
                "  ON m.id = dm.movie_id " +
                "WHERE dm.director_id = ?;";
        List<Film> films = jdbcTemplate.query(sqlReadQuery, this::mapRowToFilm, directorId);

        if (sortBy.equals("year")) {
            films = films.stream()
                    .sorted(Comparator.comparing(Film::getReleaseDate))
                    .collect(Collectors.toList());
        } else if (sortBy.equals("likes")) {
            films = films.stream()
                    .sorted((film1, film2) -> (film1.getUsersLikes().size() - film2.getUsersLikes().size()) * -1)
                    .collect(Collectors.toList());
        }

        log.info("Получены фильмы из базы данных из таблицы movies для режисера с id {}. Отсортированы по {} \n {}",
                directorId, sortBy, films);
        return films;
    }

    @Override
    public Director addDirector(Director director) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("directors")
                .usingGeneratedKeyColumns("id");

        Integer directorIdFromDbAfterInsert = simpleJdbcInsert.executeAndReturnKey(director.toMap()).intValue();
        director.setId(directorIdFromDbAfterInsert);

        log.info("Режисер добавлен в базу данных в таблицу directors по ID: {} \n {}",
                directorIdFromDbAfterInsert, director);
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        int directorId = director.getId();
        String sql = "UPDATE directors SET name = ? WHERE id = ?";

        jdbcTemplate.update(sql, director.getName(), directorId);

        Director updatedDirector = getDirectorById(directorId);
        log.info("Данные режисера с id {} успешно обновлены в базе данных по таблице directors. \n {}",
                directorId, updatedDirector);
        return updatedDirector;
    }

    @Override
    public Director deleteDirectorById(Integer directorId) {
        Director currentDirector = getDirectorById(directorId);
        if (currentDirector == null) {
            throw new DirectorNotFoundException("Режисер с id " + directorId + " не существует!");
        }
        String sqlDeleteDirector= "DELETE FROM directors WHERE id = ?";

        jdbcTemplate.update(sqlDeleteDirector, directorId);
        log.info("Режисер с ID: {} удален из базы данных в таблице directors",
                directorId);
        return currentDirector;
    }

    @Override
    public List<Director> getAllDirectors() {
        String sqlReadQuery = "SELECT * FROM directors";
        List<Director> directors = jdbcTemplate.query(sqlReadQuery, this::mapRowToDirector);
        log.info("Получен список режисеров из базы данных по таблице directors. \n {}", directors);
        return directors;
    }

    @Override
    public Director getDirectorById(Integer directorId) {
        String sql = "SELECT * FROM directors WHERE id = ?";
        SqlRowSet rows = jdbcTemplate.queryForRowSet(sql, directorId);

        if (rows.next()) {
            Director director = Director.builder()
                    .id(rows.getInt("id"))
                    .name(rows.getString("name"))
                    .build();

            log.info("Из базы данных получен режисер по ID: {} в таблице directors. \n {}", directorId, director);

            return director;
        } else {
            throw new DirectorNotFoundException(String.format("Режисер с id: %d не найден", directorId));
        }
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
                .directors(getDirectorsFromDb(filmId))
                .build();
    }

    private Director mapRowToDirector(ResultSet rs, int rowNum) throws SQLException {
        return Director.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
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
//        удаление информации о фильме-жанрах перед добавлением новой информации
        String sqlDeleteMovieGenre = "DELETE FROM movie_genre WHERE movie_id = ?";

        jdbcTemplate.update(sqlDeleteMovieGenre, film.getId());
        log.info("Фильм по ID: {} и его жанр удалены из базы данных в таблице movie_genre.",
                film.getId());

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

    private void addDirectorsToDb(final Film film) {
        List<Director> newDirectors = new ArrayList<>(film.getDirectors());
        Integer filmId = film.getId();
        List<Director> oldDirectors = getDirectorsFromDb(filmId);

        String sqlDeleteMovieDirector = "DELETE FROM director_movies WHERE director_id = ? and movie_id = ?";
        jdbcTemplate.batchUpdate(sqlDeleteMovieDirector, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Director director = oldDirectors.get(i);
                ps.setInt(1, director.getId());
                ps.setInt(2, filmId);
            }

            @Override
            public int getBatchSize() {
                return oldDirectors.size();
            }
        });

        log.info("Удалена связь режисеров с фильмом по id: {} в таблице director_movies.",
                film.getId());

        String sql = "INSERT INTO director_movies (director_id, movie_id) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Director director = newDirectors.get(i);
                ps.setInt(1, director.getId());
                ps.setInt(2, filmId);
            }

            @Override
            public int getBatchSize() {
                return newDirectors.size();
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

    private List<Director> getDirectorsFromDb(final Integer filmId) {
        String sql = "SELECT d.id, d.name FROM director_movies dm " +
                "JOIN directors d ON dm.director_id = d.id " +
                "WHERE dm.movie_id = ?";

        return new ArrayList<>(jdbcTemplate.query(sql, this::mapRowToDirector, filmId));
    }

    @Override
    public boolean isFilmExist(Integer filmId) {
        String sql = "SELECT COUNT(id) FROM movies WHERE id = ?";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, filmId);
        rs.next();
        return rs.getInt(1) > 0;
    }

    @Override
    public void remove(Integer filmId) {
        String sqlDeleteFilm = "DELETE FROM movies WHERE id = ?";

        jdbcTemplate.update(sqlDeleteFilm, filmId);
        log.info("Фильм по ID: {} и лайки пользователей удалены из базы данных в таблицах movies, movie_like.",
                filmId);
    }
}
