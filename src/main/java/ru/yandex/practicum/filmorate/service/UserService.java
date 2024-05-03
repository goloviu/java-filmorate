package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(@Valid @RequestBody User user) {
        isValidUser(user);
        return userStorage.addUser(user);
    }

    public User updateUser(@Valid @RequestBody User user) {
        isValidUser(user);
        return userStorage.updateUser(user);
    }

    public User addToFriends(final Integer userId, final Integer friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        if (user.getFriendsRequests().contains(friendId)) {
            user.getFriendsRequests().remove(friendId);
            user.getFriends().add(friendId);
        }

        friend.getFriendsRequests().add(userId);
        user.getFriends().add(friendId);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
        return friend;
    }

    public User removeFromFriends(final Integer userId, final Integer otherUserId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(otherUserId);

        user.getFriends().remove(otherUserId);
        user.getFriendsRequests().remove(otherUserId);
        friend.getFriends().remove(userId);
        friend.getFriendsRequests().remove(userId);

        userStorage.removeFriendById(userId, otherUserId);
        return friend;
    }

    public List<User> getCommonFriends(final Integer userId, final Integer otherUserId) {
        User user = userStorage.getUserById(userId);
        User otherUser = userStorage.getUserById(otherUserId);

        return user.getFriends().stream()
                .filter(friendId -> otherUser.getFriends().stream()
                        .anyMatch(otherUserFriendId -> friendId.equals(otherUserFriendId)))
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public User getUserById(final Integer userId) {
        return userStorage.getUserById(userId);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public List<User> getUserFriends(final Integer userId) {
        return userStorage.getUserById(userId).getFriends().stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public void isValidUser(User user) {
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Пустое имя пользователя заменено на логин: {}", user);
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.debug("Дата рождения не может быть в будущем: {}", user);
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    public void deleteUserById(Integer userId) {
        if (!userStorage.isUserExist(userId)) {
            throw new UserNotFoundException("Пользователь не найден: " + userId);
        }
        userStorage.removeUser(userId);
    }
}
