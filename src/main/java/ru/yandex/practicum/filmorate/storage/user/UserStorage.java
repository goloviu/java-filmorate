package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User addUser(final User user);

    User removeUser(final User user);

    User updateUser(final User user);

    User getUserById(final Integer userId);

    List<User> getAllUsers();

    boolean isUserExist(final Integer userId);
}
