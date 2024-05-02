package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
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
        userStorage.saveUserFeed(userId, 3, 1, friendId);
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
        userStorage.saveUserFeed(userId, 3, 2, otherUserId);
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

    public List<Feed> getFeedByUserId(final Integer userId) {
        return userStorage.getFeedByUserId(userId);
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

    public List<Film> getRecommendedFilms(Integer userId) {
        if (!userStorage.isUserExist(userId)) {
            throw new UserNotFoundException("Пользователь не найден: " + userId);
        }
        Map<Integer, List<Integer>> allLikes = userStorage.getLikes();
        List<Integer> filmsUserLiked = allLikes.get(userId);
        Integer maxCoincidence = 0; // подсчет максимального количества совпадающих лайков на фильмах
        List<Integer> recommendedIdFilms = new ArrayList<>();

        for (Integer otherUserId : allLikes.keySet()) { // для каждого пользователя подсчет совпадающих лайков на фильмах и сохранение максимального
            if (!otherUserId.equals(userId)) {
                List<Integer> filmsOtherUserLiked = new ArrayList<>(allLikes.get(otherUserId));
                filmsOtherUserLiked.retainAll(filmsUserLiked);
                if (filmsOtherUserLiked.size() > maxCoincidence) {
                    List<Integer> otherUserCommonLikes = new ArrayList<>(allLikes.get(otherUserId));
                    otherUserCommonLikes.removeAll(filmsUserLiked);
                    recommendedIdFilms = new ArrayList<>(otherUserCommonLikes);
                }
            }
        }
        List<Film> recommendedFilms = new ArrayList<>();
        if (!recommendedIdFilms.isEmpty()) {
            List<Film> films = filmStorage.getAllFilms();
            for (Film film : films) {
                if (recommendedIdFilms.contains(film.getId())) {
                    recommendedFilms.add(film);
                }
            }
        }
        return recommendedFilms;
    }

    public void deleteUserById(Integer userId) {
        if (!userStorage.isUserExist(userId)) {
            throw new UserNotFoundException("Пользователь не найден: " + userId);
        }
        userStorage.removeUser(userId);
    }
}
