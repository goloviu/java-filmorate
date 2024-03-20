package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NullDataException;
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
        log.info("Получен POST запрос на добавление нового пользователя в базу данных");
        nullableUser(user);
        return userService.addUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Получен PUT запрос на обновление пользователя");
        nullableUser(user);
        return userService.updateUser(user);
    }

    @GetMapping
    public List<User> getUsers() {
        log.info("Получен GET запрос на нахождение всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public User getFilmById(@PathVariable Integer userId) {
        log.info("Получен GET запрос на нахождение пользователя по ID");
        return userService.getUserById(userId);
    }

    @PutMapping("/{userId}/friends/{otherUserId}")
    public User addToFriend(@PathVariable Integer userId, @PathVariable Integer otherUserId) {
        log.info("Получен PUT запрос на добавление в друзья");
        return userService.addToFriends(userId, otherUserId);
    }

    @DeleteMapping("/{userId}/friends/{otherUserId}")
    public User removeFromFriends(@PathVariable Integer userId, @PathVariable Integer otherUserId) {
        log.info("Получен DELETE запрос на удаление из друзей");
        return userService.removeFromFriends(userId, otherUserId);
    }

    @GetMapping("/{userId}/friends")
    public List<User> getAllUserFriends(@PathVariable Integer userId) {
        log.info("Получен GET запрос на получения списка друзей пользователя");
        return userService.getUserFriends(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherUserId}")
    public List<User> getCommonFriendsWithOtherUser(@PathVariable Integer userId, @PathVariable Integer otherUserId) {
        log.info("Получен GET запрос на получения списка общих друзей с другим пользователем");
        return userService.getCommonFrineds(userId, otherUserId);
    }

    private void nullableUser(final User user) {
        if (user == null) {
            throw new NullDataException("Данные пользователя не указаны");
        }
    }
}
