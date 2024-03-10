package ru.yandex.practicum.filmorate.controller;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private int userId = 1;
    private HashMap<Integer, User> users = new HashMap<>();

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        if (user.getId() != null && users.containsValue(user)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        isValidUser(user);
        user.setId(generateUserId());
        users.put(user.getId(), user);
        log.info("Добавлен пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
       if (user.getId() == null || !users.containsKey(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        isValidUser(user);
        User notUpdatedUser = users.get(user.getId());
        users.put(user.getId(), user);
        log.info("Информация о пользователе обновлена:\n Было: {} \n Стало: {}\n", notUpdatedUser, user);
        return user;
    }

    @GetMapping
    public List<User> getUsers() {
        log.info("Получен GET запрос на нахождение всех пользователей");
        return new ArrayList<>(users.values());
    }

    private Integer generateUserId() {
        return userId++;
    }

    protected void isValidUser(User user) {
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Пустое имя пользователя заменено на логин: {}", user);
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.debug("Дата рождения не может быть в будущем: {}", user);
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
