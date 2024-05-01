package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reviews")
@Slf4j
public class ReviewsController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewsController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        log.info("Получен POST запрос на добавление нового отзыва в базу данных: {}", review);
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("Получен PUT запрос за обновление информации об отзыве. Было:\n{}\n Стало:\n {}",
                reviewService.getReviewById(review.getReviewId()), review);
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{reviewId}")
    public Review deleteReviewById(@PathVariable Integer reviewId) {
        log.info("Получен DELETE запрос на удаление отзыва по ID: {}", reviewId);
        return reviewService.deleteReviewById(reviewId);
    }

    @GetMapping("/{reviewId}")
    public Review getReviewById(@PathVariable Integer reviewId) {
        log.info("Получен GET запрос на нахождения отзыва по ID: {}", reviewId);
        return reviewService.getReviewById(reviewId);
    }

    @GetMapping
    public List<Review> getReviewsByFilmId(@RequestParam Map<String, String> requestParams) {
        log.info("Получен GET запрос на нахождения всех {} отзывов по ID фильма: {}",
                requestParams.getOrDefault("count", "10"),
                requestParams.getOrDefault("filmId", "-1"));
        return reviewService.getReviewsByFilmId(requestParams);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public Review addLikeToReview(@PathVariable Integer reviewId, @PathVariable Integer userId) {
        log.info("Получен PUT запрос на добавление лайка к отзыву по ID: {} Пользователем по ID: {}", reviewId, userId);
        return reviewService.addLikeToReview(reviewId, userId);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public Review addDislikeToReview(@PathVariable Integer reviewId, @PathVariable Integer userId) {
        log.info("Получен PUT запрос на добавление дизлайка к отзыву по ID: {} Пользователем по ID: {}", reviewId, userId);
        return reviewService.addDislikeToReview(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public Review deleteLikeToReview(@PathVariable Integer reviewId, @PathVariable Integer userId) {
        log.info("Получен DELETE запрос на удаление лайка к отзыву по ID: {} Пользователем по ID: {}", reviewId, userId);
        return reviewService.deleteLikeToReview(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public Review deleteDislikeToReview(@PathVariable Integer reviewId, @PathVariable Integer userId) {
        log.info("Получен DELETE запрос на удаление дизлайка к отзыву по ID: {} Пользователем по ID: {}", reviewId, userId);
        return reviewService.deleteDislikeToReview(reviewId, userId);
    }
}