package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    public void newController() {
        userService = new UserService(new UserDbStorage(new JdbcTemplate()));
    }

    @Test
    public void testisValidUserShouldUseLoginWhenNameIsNull() {
        // given
        User user = new User(0, "foo@bar.test", "test", "", LocalDate.parse("1999-09-05"),
                Collections.emptySet(), Collections.emptySet());

        // do
        assertTrue(user.getName().isEmpty(), "Имя пользователя должно быть пустое");
        assertEquals("test", user.getLogin(), "Логин должен быть test");
        userService.isValidUser(user);

        // expect
        assertEquals("test", user.getName(), "Пустое имя должно замениться на логин: " + user.getLogin());
        assertTrue(user.getName() == user.getLogin(), "Логин и имя пользователя не совпадают");
    }

    @Test
    public void testisValidUserShouldNotReplaceLoginToNameWhenNameIsNotNull() {
        // given
        User user = new User(0, "foo@bar.test", "foo", "bar", LocalDate.parse("1999-09-05"),
                Collections.emptySet(), Collections.emptySet());

        // do
        assertFalse(user.getName().isEmpty(), "Имя пользователя не должно быть пустое");
        assertEquals("foo", user.getLogin(), "Логин должен быть foo до валидации");
        assertEquals("bar", user.getName(), "Имя должно быть bar до валидации");
        assertNotEquals(user.getName(), user.getLogin(), "Логин и имя не должны совпадать");
        userService.isValidUser(user);

        // expect
        assertEquals("bar", user.getName(), "Логин не должен заменять имя. Логин: "
                + user.getLogin() + " Имя: " + user.getName());
        assertNotEquals(user.getName(), user.getLogin(), "Логин и имя не должны совпадать");
        assertTrue(user.getName() != user.getLogin(), "Логин и имя пользователя совпадают");
        assertEquals("foo", user.getLogin(), "Логин должен быть foo после валидации");
        assertEquals("bar", user.getName(), "Имя должно быть bar после валидации");
    }

    @Test
    public void testisValidUserShouldThrowValidationExceptionWhenUserBirthdayInFuture() {
        // given
        User user = new User(0, "foo@bar.test", "foo", "bar", LocalDate.now().plusDays(1),
                Collections.emptySet(), Collections.emptySet());

        // expect
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.isValidUser(user),
                "Исключение не выбросилось");
        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    public void testisValidUserShouldNotThrowValidationExceptionWhenUserBirthdayInPast() {
        // given
        User user = new User(0, "foo@bar.test", "foo", "bar", LocalDate.now().minusDays(1),
                Collections.emptySet(), Collections.emptySet());

        // expect
        assertDoesNotThrow(() -> userService.isValidUser(user),
                "Исключение не должно выбрасываться когда день рождение в укаано в прошлом");
    }
}