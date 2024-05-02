package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exceptions.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReviewDbStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private ReviewDbStorage reviewDbStorage;
    private UserDbStorage userDbStorage;
    private FilmDbStorage filmDbStorage;

    @BeforeEach
    public void makeNewReviewStorage() {
        userDbStorage = new UserDbStorage(jdbcTemplate);
        filmDbStorage = new FilmDbStorage(jdbcTemplate);
        this.reviewDbStorage = new ReviewDbStorage(jdbcTemplate);
    }

    private Review makeReviewWithoutId(Integer userNum) {
        User user = new User(0, "user" + userNum + "@mail.ru", "user" + userNum, "userName" + userNum,
                LocalDate.parse("2000-12-27"), new HashSet<>(), new HashSet<>());
        Film film = new Film(0, "Java Developer", "About strong Java developer",
                LocalDate.parse("1895-12-28"), 60, new FilmRating(1), Collections.emptySet(), Collections.emptySet());

        return Review.builder()
                .content("Content")
                .isPositive(true)
                .userId(userDbStorage.addUser(user).getId())
                .filmId(filmDbStorage.add(film).getId())
                .useful(0)
                .build();
    }

    @Test
    void testAddShouldSaveReviewToDbWhenReviewIsNotNull() {
        //given
        Review review = makeReviewWithoutId(0);
        //do
        Integer reviewId = reviewDbStorage.add(review).getReviewId();
        Review savedReview = reviewDbStorage.getReviewById(reviewId);
        //expect
        assertThat(savedReview)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review);
    }

    @Test
    void testRemoveShouldDeleteReviewId3WhenReviewIsNotNullAndExistInTable() {
        //given
        Review review1 = makeReviewWithoutId(1);
        Review review2 = makeReviewWithoutId(2);
        Review review3 = makeReviewWithoutId(3);
        reviewDbStorage.add(review1);
        reviewDbStorage.add(review2);
        reviewDbStorage.add(review3);
        //do
        reviewDbStorage.remove(review3.getReviewId());
        //expect
        String sql = "SELECT COUNT(id) FROM reviews";
        Integer columnNum = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> (rs.getInt(1)));
        assertEquals(2, columnNum, "Количество записей больше 2х");

        ReviewNotFoundException exception = assertThrows(ReviewNotFoundException.class,
                () -> reviewDbStorage.getReviewById(review3.getReviewId()), "Исключение не выброшено");
        assertEquals("Отзыв не найден: " + review3.getReviewId(), exception.getMessage());
    }

    @Test
    void testUpdateShouldChangeReviewContentIsPositiveBySameIdWhenReviewIsNotNullAndExistInTable() {
        //given
        Review review = makeReviewWithoutId(0);
        Integer reviewId = reviewDbStorage.add(review).getReviewId();

        Review reviewForUpdate = makeReviewWithoutId(1);
        reviewForUpdate.setReviewId(reviewId);
        reviewForUpdate.setContent("Content updated");
        reviewForUpdate.setIsPositive(false);
        //do
        reviewDbStorage.update(reviewForUpdate);
        Review updatedReview = reviewDbStorage.getReviewById(reviewId);
        //expect
        assertThat(updatedReview)
                .isNotNull()
                .usingRecursiveComparison()
                .isNotEqualTo(review);
        assertEquals("Content updated", updatedReview.getContent(), "Content не совпадают");
        assertEquals(false, updatedReview.getIsPositive(), "IsPositive не совпадают");
    }

    @Test
    void testGetReviewByIdShouldReturnSavedReviewWhenReviewIsNotNull() {
        //given
        Review review = makeReviewWithoutId(0);
        Integer reviewId = reviewDbStorage.add(review).getReviewId();
        //do
        Review savedReview = reviewDbStorage.getReviewById(reviewId);
        //expect
        assertThat(savedReview)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(review);
    }

    @Test
    void testGetAllReviewsShouldReturnListOf3ReviewsWhenReviewsAreNotNull() {
        //given
        Review review1 = makeReviewWithoutId(0);
        Review review2 = makeReviewWithoutId(1);
        Review review3 = makeReviewWithoutId(2);

        reviewDbStorage.add(review1);
        reviewDbStorage.add(review2);
        reviewDbStorage.add(review3);

        List<Review> reviews = List.of(review1, review2, review3);
        //do
        List<Review> savedReviews = reviewDbStorage.getReviewsByFilmId(10, -1);
        //expect
        assertEquals(3, reviews.size(), "Размер списка отзывов не совпадает");
        assertThat(savedReviews)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(reviews);
    }
}
