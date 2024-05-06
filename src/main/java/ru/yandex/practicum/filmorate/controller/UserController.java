package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        log.info("Получен POST запрос на добавление нового пользователя в базу данных: {}", user);
        return userService.addUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Получен PUT запрос на обновление пользователя. Было:\n{}\n Стало:\n {}",
                userService.getUserById(user.getId()), user);
        return userService.updateUser(user);
    }

    @GetMapping
    public List<User> getUsers() {
        log.info("Получен GET запрос на нахождение всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Integer userId) {
        log.info("Получен GET запрос на нахождение пользователя по ID: {}", userId);
        return userService.getUserById(userId);
    }

    @PutMapping("/{userId}/friends/{otherUserId}")
    public User addToFriends(@PathVariable Integer userId, @PathVariable Integer otherUserId) {
        log.info("Получен PUT запрос на добавления в друзья пользователя ID: {} от пользователя ID: {}",
                otherUserId, userId);
        return userService.addToFriends(userId, otherUserId);
    }

    @DeleteMapping("/{userId}/friends/{otherUserId}")
    public User removeFromFriends(@PathVariable Integer userId, @PathVariable Integer otherUserId) {
        log.info("Получен DELETE запрос на удаление из друзей пользователя ID: {} от пользователя ID: {}",
                otherUserId, userId);
        return userService.removeFromFriends(userId, otherUserId);
    }

    @GetMapping("/{userId}/friends")
    public List<User> getAllUserFriends(@PathVariable Integer userId) {
        log.info("Получен GET запрос на получения списка друзей пользователя ID: {}", userId);
        return userService.getUserFriends(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherUserId}")
    public List<User> getCommonFriendsWithOtherUser(@PathVariable Integer userId, @PathVariable Integer otherUserId) {
        log.info("Получен GET запрос на получения списка общих друзей с пользователем ID: {} от пользователя ID: {}",
                otherUserId, userId);
        return userService.getCommonFriends(userId, otherUserId);
    }

    @GetMapping("/{userId}/feed")
    public List<Feed> getFeedByUserId(@PathVariable Integer userId) {
        log.info("Получен GET запрос на получение доски событий пользователя ID {}", userId);
        return userService.getFeedByUserId(userId);
    }

    @GetMapping("/{userId}/recommendations")
    public List<Film> getRecommendedFilmsForUser(@PathVariable Integer userId) {
        log.info("Получен GET запрос на получения списка рекомендованных фильмов для пользователя: {} ",
                userId);
        return userService.getRecommendedFilms(userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Integer userId) {
        log.info("Получен DELETE запрос на удаление пользователя по ID: {}", userId);
        userService.deleteUserById(userId);
    }
}
