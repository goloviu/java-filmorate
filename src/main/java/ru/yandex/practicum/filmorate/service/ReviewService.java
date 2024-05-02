package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReviewService {
    private final ReviewDbStorage reviewDbStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Autowired
    public ReviewService(ReviewDbStorage reviewDbStorage,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.reviewDbStorage = reviewDbStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Review addReview(Review review) {
        checkValid(review);
        return reviewDbStorage.add(review);
    }

    public Review updateReview(Review review) {
        checkValid(review);
        return reviewDbStorage.update(review);
    }

    public Review deleteReviewById(Integer reviewId) {
        return reviewDbStorage.remove(reviewId);
    }

    public Review getReviewById(Integer reviewId) {
        return reviewDbStorage.getReviewById(reviewId);
    }

    public List<Review> getReviewsByFilmId(Map<String, String> requestParams) {
        Integer count = Integer.parseInt(requestParams.getOrDefault("count", "10"));
        Integer filmId = Integer.parseInt(requestParams.getOrDefault("filmId", "-1"));
        return reviewDbStorage.getReviewsByFilmId(count, filmId);
    }

    public Review addLikeToReview(Integer reviewId, Integer userId) {
        return reviewDbStorage.addUserLikeToReview(userId, reviewId);
    }

    public Review addDislikeToReview(Integer reviewId, Integer userId) {
        return reviewDbStorage.addUserDislikeToReview(userId, reviewId);
    }

    public Review deleteLikeToReview(Integer reviewId, Integer userId) {
        return reviewDbStorage.removeUserLike(userId, reviewId);
    }

    public Review deleteDislikeToReview(Integer reviewId, Integer userId) {
        return reviewDbStorage.removeUserDislike(userId, reviewId);
    }

    public void checkValid(Review review) {
        if (!userStorage.isUserExist(review.getUserId())) {
            throw new UserNotFoundException("Пользователь не найден: " + review.getUserId());
        }
        if (!filmStorage.isFilmExist(review.getFilmId())) {
            throw new FilmNotFoundException("Фильм не найден: " + review.getFilmId());
        }
    }
}
