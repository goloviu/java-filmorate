package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.UserAlreadyExistException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private int userId = 1;
    private final HashMap<Integer, User> users = new HashMap<>();

    @Override
    public User addUser(final User user) {
        checkExistUserForAddUser(user);

        user.setId(generateUserId());
        users.put(user.getId(), user);
        log.info("Добавлен пользователь: {}", user);
        return user;
    }

    @Override
    public User removeUser(final User user) {
        if (user != null) {
            users.remove(user.getId());
        }
        return user;
    }

    @Override
    public User updateUser(final User user) {
        checkExistUserForUpdateUser(user);

        User notUpdatedUser = users.get(user.getId());
        users.put(user.getId(), user);
        log.info("Информация о пользователе обновлена:\n Было: {} \n Стало: {}\n", notUpdatedUser, user);
        return user;
    }

    public User getUserById(final Integer userId) {
        if (!users.containsKey(userId)) {
            throw new UserNotFoundException("Пользователь не найден: " + userId);
        }
        return users.get(userId);
    }

    public List<User> getAllUsers() {
        log.info("Список всех пользователей передан в сервис");
        return new ArrayList<>(users.values());
    }

    private Integer generateUserId() {
        return userId++;
    }

    private void checkExistUserForAddUser(final User user) {
        if (user.getId() != null && users.containsValue(user)) {
            throw new UserAlreadyExistException("Пользователь уже существует: " + user.getId());
        }
    }

    private void checkExistUserForUpdateUser(final User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            throw new UserNotFoundException("Пользователь не найден: " + user.getId());
        }
    }

    public boolean isUserExist(final Integer userId) {
        return users.containsKey(userId);
    }
}
