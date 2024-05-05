package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserStorage {

    User addUser(final User user);

    User removeUser(final User user);

    boolean removeFriendById(final Integer userId, final Integer friendId);

    User updateUser(final User user);

    User getUserById(final Integer userId);

    List<User> getAllUsers();

    boolean isUserExist(final Integer userId);

    Set<Integer> getUserLikes(final Integer userId);

    List<Feed> getFeedByUserId(final Integer userId);

    boolean saveUserFeed(final Integer userId, final EventType eventType, final OperationType operationType,
                         final Integer entityId);

    Map<Integer, List<Integer>> getLikes();
}
