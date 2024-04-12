package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserDbStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private UserDbStorage userStorage;

    @BeforeEach
    private void newUserStorage() {
        this.userStorage = new UserDbStorage(jdbcTemplate);
    }

    private User makeUserWithId() {
        return User.builder()
                .id(1)
                .name("Test name")
                .login("Test login")
                .email("foo@bar.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .friends(Collections.emptySet())
                .friendsRequests(Collections.emptySet())
                .build();
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

    @Test
    void testAddUser_ShouldSaveUserToDb_WhenUserIsNotNull() {
        //given
        User user = makeUserWithId();
        //do
        userStorage.addUser(user);
        User savedUser = userStorage.getUserById(1);
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
        Integer columnNum =  jdbcTemplate.queryForObject(sql, (rs, rowNum) -> (rs.getInt(1)));
        assertEquals(2, columnNum, "Количество записей больше 2х");

        EmptyResultDataAccessException exception = assertThrows(EmptyResultDataAccessException.class
                , () -> userStorage.getUserById(3), "Исключение не выброшено");
        assertEquals("Incorrect result size: expected 1, actual 0", exception.getMessage());
    }

    @Test
    void testUpdateUser_ShouldChangeNewUserNameBySameUserId_WhenUserIsNotNull() {
        //given
        User user = makeUserWithoutId();
        User userForUpdate = makeUserWithId();
        userForUpdate.setName("UpdatedName");
        //do
        userStorage.addUser(user);
        userStorage.updateUser(userForUpdate);
        User updatedUser = userStorage.getUserById(1);
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
        userStorage.addUser(user);
        //do
        User savedUser = userStorage.getUserById(1);
        //expect
        assertThat(savedUser)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(user);
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
}