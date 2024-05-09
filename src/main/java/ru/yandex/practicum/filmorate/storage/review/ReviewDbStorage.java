package ru.yandex.practicum.filmorate.storage.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

import java.util.stream.Collectors;

@Component
@Slf4j
public class ReviewDbStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Review add(Review review) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("id");

        Integer reviewIdFromDbAfterInsert = simpleJdbcInsert.executeAndReturnKey(review.toMap()).intValue();
        review.setReviewId(reviewIdFromDbAfterInsert);
        updateUsefulness(review);

        log.info("Отзыв добавлен в базу данных в таблицу reviews по ID: {} \n {}", reviewIdFromDbAfterInsert, review);
        return review;
    }

    public Review remove(Integer reviewId) {
        Review review = getReviewById(reviewId);
        String sqlDeleteReview = "DELETE FROM reviews WHERE id = ?";

        jdbcTemplate.update(sqlDeleteReview, reviewId);
        log.info("Отзыв по ID: {} удален из базы данных в таблице reviews.",
                reviewId);
        return review;
    }

    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ?" +
                "WHERE id = ?";

        jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), review.getReviewId());

        String sqlReadQuery = "SELECT * FROM reviews WHERE id = ?";
        Review updatedReview = jdbcTemplate.queryForObject(sqlReadQuery, this::mapRowToReview, review.getReviewId());
        log.info("Отзыв успешно обновлен в базе данных по таблице reviews. \n {}", updatedReview);

        return updatedReview;
    }

    public Review getReviewById(Integer reviewId) {
        if (!isReviewExist(reviewId)) {
            throw new ReviewNotFoundException("Отзыв не найден: " + reviewId);
        }

        String sqlReadQuery = "SELECT * FROM reviews WHERE id = ?";
        Review review = jdbcTemplate.queryForObject(sqlReadQuery, this::mapRowToReview, reviewId);
        log.info("Получен отзыв из базы данных по таблице reviews. \n {}", review);
        return review;
    }

    public List<Review> getReviewsByFilmId(Integer count, Integer filmId) {
        List<Review> reviews;

        if (filmId != -1) {
            String sqlReadQuery = "SELECT * FROM reviews WHERE movie_id = ?";
            reviews = jdbcTemplate.query(sqlReadQuery, this::mapRowToReview, filmId);
            log.info("Получен список отзывов из базы данных по таблице reviews для фильма ID: {}", filmId);
        } else {
            String sqlReadQuery = "SELECT * FROM reviews";
            reviews = jdbcTemplate.query(sqlReadQuery, this::mapRowToReview);
            log.info("Получен список всех отзывов из базы данных по таблице reviews");
        }

        return reviews.stream()
                .sorted(Comparator.comparing(Review::getUseful).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public Review addUserLikeToReview(Integer userId, Integer reviewId) {
//        удаляем лайк/дизлайк, если был добавлен раннее на пару отзыв-пользователь
        removeUserLike(userId, reviewId);
        removeUserDislike(userId, reviewId);

        String sql = "INSERT INTO review_like (review_id, user_id) " +
                "VALUES(?, ?)";

        boolean isAddedLikeToReview = jdbcTemplate.update(sql, reviewId, userId) > 1;
        if (isAddedLikeToReview) {
            log.info("Пользователь ID {} поставил лайк отзыву ID {}", userId, reviewId);
        }

        updateUsefulness(getReviewById(reviewId));
        return getReviewById(reviewId);
    }

    public Review addUserDislikeToReview(Integer userId, Integer reviewId) {
//        удаляем лайк/дизлайк, если был добавлен раннее на пару отзыв-пользователь
        removeUserLike(userId, reviewId);
        removeUserDislike(userId, reviewId);

        String sql = "INSERT INTO review_dislike (review_id, user_id) " +
                "VALUES(?, ?)";

        boolean isAddedDislikeToReview = jdbcTemplate.update(sql, reviewId, userId) > 1;
        if (isAddedDislikeToReview) {
            log.info("Пользователь ID {} поставил дизлайк отзыву ID {}", userId, reviewId);
        }

        updateUsefulness(getReviewById(reviewId));
        return getReviewById(reviewId);
    }

    public Review removeUserLike(final Integer userId, final Integer reviewId) {
        String sql = "DELETE FROM review_like WHERE review_id = ? AND user_id = ?";

        boolean isLikeRemoved = jdbcTemplate.update(sql, reviewId, userId) > 1;
        if (isLikeRemoved) {
            log.info("Пользователь ID {} удалил лайк у отзыва ID {}", userId, reviewId);
        }

        updateUsefulness(getReviewById(reviewId));
        return getReviewById(reviewId);
    }

    public Review removeUserDislike(final Integer userId, final Integer reviewId) {
        String sql = "DELETE FROM review_dislike WHERE review_id = ? AND user_id = ?";

        boolean isDislikeRemoved = jdbcTemplate.update(sql, reviewId, userId) > 1;
        if (isDislikeRemoved) {
            log.info("Пользователь ID {} удалил дизлайк у отзыва ID {}", userId, reviewId);
        }

        updateUsefulness(getReviewById(reviewId));
        return getReviewById(reviewId);
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        Integer id = rs.getInt("id");

        return Review.builder()
                .reviewId(id)
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .userId(rs.getInt("user_id"))
                .filmId(rs.getInt("movie_id"))
                .useful(rs.getInt("useful"))
                .build();
    }

    public Integer countUsefulness(Integer reviewId) {
        String sqlLikes = "SELECT COUNT(*) FROM review_like WHERE review_id = ?";
        String sqlDislikes = "SELECT COUNT(*) FROM review_dislike WHERE review_id = ?";

        Integer countLikes = jdbcTemplate.queryForObject(sqlLikes, Integer.class, reviewId);
        Integer countDislikes = jdbcTemplate.queryForObject(sqlDislikes, Integer.class, reviewId);
        Integer usefulness = countLikes - countDislikes;
        log.info("У отзыва ID {}: {} лайков, {} дизлайков -> полезность - {}", reviewId, countLikes, countDislikes,
                usefulness);

        return usefulness;
    }

    public void updateUsefulness(Review review) {
        log.info("У отзыва ID {}: старая полезность - {}, возможно новая полезность - {}", review.getReviewId(),
                review.getUseful(), countUsefulness(review.getReviewId()));
        if (review.getUseful() == null || !review.getUseful().equals(countUsefulness(review.getReviewId()))) {
            review.setUseful(countUsefulness(review.getReviewId()));
            String sql = "UPDATE reviews SET useful = ? WHERE id = ?";
            jdbcTemplate.update(sql, review.getUseful(), review.getReviewId());
        }
    }

    public boolean isReviewExist(Integer reviewId) {
        String sql = "SELECT COUNT(id) FROM reviews WHERE id = ?";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, reviewId);
        rs.next();
        return rs.getInt(1) > 0;
    }
}
