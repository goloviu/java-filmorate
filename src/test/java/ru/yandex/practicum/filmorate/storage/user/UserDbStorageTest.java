package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private UserDbStorage userStorage;
    private FilmDbStorage filmDbStorage;

    @BeforeEach
    private void newUserStorage() {
        this.userStorage = new UserDbStorage(jdbcTemplate);
        this.filmDbStorage = new FilmDbStorage(jdbcTemplate);
    }

    private User makeUserWithoutId() {
        return User.builder()
                .name("Test name")
                .login("Test login")
                .email("foo@bar.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .friends(Collections.emptySet())
                .friendsRequests(Collections.emptySet())
                .build();
    }

    public Film makeFilmWithoutId() {
        return Film.builder()
                .name("Java and a bit more about coffee")
                .description("about coffee")
                .releaseDate(LocalDate.of(2024, 04, 11))
                .duration(200)
                .mpa(new FilmRating(1, "G"))
                .genres(Collections.emptySet())
                .usersLikes(Collections.emptySet())
                .build();
    }

    @Test
    void testAddUser_ShouldSaveUserToDb_WhenUserIsNotNull() {
        //given
        User user = makeUserWithoutId();
        //do
        Integer userId = userStorage.addUser(user).getId();
        User savedUser = userStorage.getUserById(userId);
        savedUser.setFriendsRequests(Collections.emptySet());
        savedUser.setFriends(Collections.emptySet());
        //expect
        assertThat(savedUser)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(user);
    }

    @Test
    void testRemoveUser_ShouldRemoveUserId3_WhenUserIsNotNullAndExistInTable() {
        //given
        User user1 = makeUserWithoutId();
        User user2 = makeUserWithoutId();
        user2.setEmail("test@mail.test");
        user2.setLogin("foo");
        User user3 = makeUserWithoutId();
        user3.setEmail("test2@mail.test");
        user3.setLogin("bar");
        //do
        userStorage.addUser(user1);
        userStorage.addUser(user2);
        userStorage.addUser(user3);
        userStorage.removeUser(user3);
        //expect
        String sql = "SELECT COUNT(id) FROM users";
        Integer columnNum = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> (rs.getInt(1)));
        assertEquals(2, columnNum, "Количество записей больше 2х");

        EmptyResultDataAccessException exception = assertThrows(EmptyResultDataAccessException.class,
                () -> userStorage.getUserById(3), "Исключение не выброшено");
        assertEquals("Incorrect result size: expected 1, actual 0", exception.getMessage());
    }

    @Test
    void testUpdateUser_ShouldChangeNewUserNameBySameUserId_WhenUserIsNotNull() {
        //given
        User user = makeUserWithoutId();
        Integer userId = userStorage.addUser(user).getId();

        User userForUpdate = makeUserWithoutId();
        userForUpdate.setId(userId);
        userForUpdate.setName("UpdatedName");
        //do
        userStorage.updateUser(userForUpdate);
        User updatedUser = userStorage.getUserById(userId);
        //expect
        assertThat(updatedUser)
                .isNotNull()
                .isNotEqualTo(user);

        assertEquals("UpdatedName", updatedUser.getName(), "Имя пользователя не совпадает с обновленным");
    }

    @Test
    void testGetUserById_ShouldReturnSavedUser_WhenUserIsNotNull() {
        //given
        User user = makeUserWithoutId();
        Integer userId = userStorage.addUser(user).getId();
        //do
        User savedUser = userStorage.getUserById(userId);
        //expect
        assertThat(savedUser)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(user);
    }

    @Test
    void testGetUserLikes_ShouldReturnNotEmptyLikes_WhenUserIsNotNull() {
        //given
        User user = makeUserWithoutId();
        Film film1 = makeFilmWithoutId();
        Film film2 = makeFilmWithoutId();
        Integer filmId1 = filmDbStorage.add(film1).getId();
        Integer filmId2 = filmDbStorage.add(film2).getId();
        Integer userId = userStorage.addUser(user).getId();
        filmDbStorage.addUserLikeToFilm(userId, filmId1);
        filmDbStorage.addUserLikeToFilm(userId, filmId2);
        //do
        Set<Integer> userLikes = userStorage.getUserLikes(userId);
        //expect
        assertEquals(2, userLikes.size(), "Размер списка фильмов не совпадает");
        assertThat(userLikes)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(userLikes);
    }

    @Test
    void testGetAllUsers_ShouldReturnListOf3SavedUsers_WhenUsersAreNotNull() {
        //given
        User user1 = makeUserWithoutId();
        User user2 = makeUserWithoutId();
        user2.setEmail("foo@test.com");
        user2.setLogin("foo");
        User user3 = makeUserWithoutId();
        user3.setEmail("bar@test.com");
        user3.setLogin("bar");

        userStorage.addUser(user1);
        userStorage.addUser(user2);
        userStorage.addUser(user3);

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        users.add(user3);
        //do
        List<User> savedUsers = userStorage.getAllUsers();

        //expect
        assertThat(savedUsers)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(users);
        assertEquals(3, users.size(), "Размер списка не совпадает");
    }

    @Test
    void testIsUserExist_ShouldReturnTrue_WhenUserIsNotNullAndSavedInDb() {
        //given
        User user = makeUserWithoutId();
        userStorage.addUser(user);
        //do
        boolean isUserExist = userStorage.isUserExist(user.getId());
        //expect
        assertThat(isUserExist)
                .isTrue();
    }

    @Test
    void testSaveUserFeed_ShouldReturnEventTypeFriendAndOperationAdd_WhenUserFeedIsNotNullAndSavedInDb() {
        //given
        User user = makeUserWithoutId();
        Integer userId = userStorage.addUser(user).getId();
        //do
        userStorage.saveUserFeed(user.getId(), EventType.FRIEND, OperationType.ADD, 2);
        Feed feedFromDb = userStorage.getFeedByUserId(userId).get(0);
        //expect
        assertEquals(EventType.FRIEND, feedFromDb.getEventType());
        assertEquals(OperationType.ADD, feedFromDb.getOperation());
    }

    @Test
    void testSaveUserFeed_ShouldReturnEventTypeFriendAndOperationRemove_WhenUserFeedIsNotNullAndSavedInDb() {
        //given
        User user = makeUserWithoutId();
        Integer userId = userStorage.addUser(user).getId();
        //do
        userStorage.saveUserFeed(user.getId(), EventType.FRIEND, OperationType.REMOVE, 2);
        Feed feedFromDb = userStorage.getFeedByUserId(userId).get(0);
        //expect
        assertEquals(EventType.FRIEND, feedFromDb.getEventType());
        assertEquals(OperationType.REMOVE, feedFromDb.getOperation());
    }

    @Test
    void testSaveUserFeed_ShouldReturnEventTypeReviewAndOperationAdd_WhenUserFeedIsNotNullAndSavedInDb() {
        //given
        User user = makeUserWithoutId();
        Integer userId = userStorage.addUser(user).getId();
        //do
        userStorage.saveUserFeed(user.getId(), EventType.REVIEW, OperationType.ADD, 2);
        Feed feedFromDb = userStorage.getFeedByUserId(userId).get(0);
        //expect
        assertEquals(EventType.REVIEW, feedFromDb.getEventType());
        assertEquals(OperationType.ADD, feedFromDb.getOperation());
    }

    @Test
    void testSaveUserFeed_ShouldReturnEventTypeReviewAndOperationRemove_WhenUserFeedIsNotNullAndSavedInDb() {
        //given
        User user = makeUserWithoutId();
        Integer userId = userStorage.addUser(user).getId();
        //do
        userStorage.saveUserFeed(user.getId(), EventType.REVIEW, OperationType.REMOVE, 2);
        Feed feedFromDb = userStorage.getFeedByUserId(userId).get(0);
        //expect
        assertEquals(EventType.REVIEW, feedFromDb.getEventType());
        assertEquals(OperationType.REMOVE, feedFromDb.getOperation());
    }

    @Test
    void testSaveUserFeed_ShouldReturnEventTypeReviewAndOperationUpdate_WhenUserFeedIsNotNullAndSavedInDb() {
        //given
        User user = makeUserWithoutId();
        Integer userId = userStorage.addUser(user).getId();
        //do
        userStorage.saveUserFeed(user.getId(), EventType.REVIEW, OperationType.UPDATE, 2);
        Feed feedFromDb = userStorage.getFeedByUserId(userId).get(0);
        //expect
        assertEquals(EventType.REVIEW, feedFromDb.getEventType());
        assertEquals(OperationType.UPDATE, feedFromDb.getOperation());
    }

    @Test
    void testSaveUserFeed_ShouldReturnEventTypeLikeAndOperationAdd_WhenUserFeedIsNotNullAndSavedInDb() {
        //given
        User user = makeUserWithoutId();
        Integer userId = userStorage.addUser(user).getId();
        //do
        userStorage.saveUserFeed(user.getId(), EventType.LIKE, OperationType.ADD, 2);
        Feed feedFromDb = userStorage.getFeedByUserId(userId).get(0);
        //expect
        assertEquals(EventType.LIKE, feedFromDb.getEventType());
        assertEquals(OperationType.ADD, feedFromDb.getOperation());
    }

    @Test
    void testSaveUserFeed_ShouldReturnEventTypeLikeAndOperationRemove_WhenUserFeedIsNotNullAndSavedInDb() {
        //given
        User user = makeUserWithoutId();
        Integer userId = userStorage.addUser(user).getId();
        //do
        userStorage.saveUserFeed(user.getId(), EventType.LIKE, OperationType.REMOVE, 2);
        Feed feedFromDb = userStorage.getFeedByUserId(userId).get(0);
        //expect
        assertEquals(EventType.LIKE, feedFromDb.getEventType());
        assertEquals(OperationType.REMOVE, feedFromDb.getOperation());
    }

    @Test
    void testGetFeedByUserId_ShouldReturnEventTypeLikeAndOperationRemove_WhenUserFeedIsNotNull() {
        //given
        User user = makeUserWithoutId();
        Integer userId = userStorage.addUser(user).getId();
        //do
        userStorage.saveUserFeed(user.getId(), EventType.LIKE, OperationType.REMOVE, 2);
        Feed feedFromDb = userStorage.getFeedByUserId(userId).get(0);
        //expect
        assertEquals(EventType.LIKE, feedFromDb.getEventType());
        assertEquals(OperationType.REMOVE, feedFromDb.getOperation());
        assertEquals(userId, feedFromDb.getUserId());
        assertEquals(2, feedFromDb.getEntityId());
    }
}