package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

public interface UserStorage {

    User addUser(final User user);

    User removeUser(final User user);

    User updateUser(final User user);
}
