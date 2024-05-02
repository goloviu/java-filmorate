package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;

public interface UserStorage {

    User addUser(final User user);

    User removeUser(final User user);

    boolean removeFriendById(final Integer userId, final Integer friendId);

    User updateUser(final User user);

    User getUserById(final Integer userId);

    List<User> getAllUsers();

    boolean isUserExist(final Integer userId);

    List<Feed> getFeedByUserId(final Integer userId);

    boolean saveUserFeed(final Integer userId, final Integer eventTypeId, final Integer operationId,
                         final Integer entityId);

    Map<Integer, List<Integer>> getLikes();

    void removeUser(Integer userId);
}
