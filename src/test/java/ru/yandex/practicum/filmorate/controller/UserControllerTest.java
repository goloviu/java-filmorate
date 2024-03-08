package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController controller;

    @BeforeEach
    public void newController() {
        controller = new UserController();
    }

    @Test
    public void testisValidUserShouldUseLoginWhenNameIsNull() {
        // given
        User user = new User(0, "foo@bar.test", "test", "", LocalDate.parse("1999-09-05"));

        // do
        assertTrue(user.getName().isEmpty(), "Имя пользователя должно быть пустое");
        assertEquals("test", user.getLogin(), "Логин должен быть test");
        controller.isValidUser(user);

        // expect
        assertEquals("test", user.getName(), "Пустое имя должно замениться на логин: " + user.getLogin());
        assertTrue(user.getName() == user.getLogin(), "Логин и имя пользователя не совпадают");
    }

    @Test
    public void testisValidUserShouldNotReplaceLoginToNameWhenNameIsNotNull() {
        // given
        User user = new User(0, "foo@bar.test", "foo", "bar", LocalDate.parse("1999-09-05"));

        // do
        assertFalse(user.getName().isEmpty(), "Имя пользователя не должно быть пустое");
        assertEquals("foo", user.getLogin(), "Логин должен быть foo до валидации");
        assertEquals("bar", user.getName(), "Имя должно быть bar до валидации");
        assertNotEquals(user.getName(), user.getLogin(), "Логин и имя не должны совпадать");
        controller.isValidUser(user);

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
        User user = new User(0, "foo@bar.test", "foo", "bar", LocalDate.now().plusDays(1));

        // expect
        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.isValidUser(user),
                "Исключение не выбросилось");
        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    public void testisValidUserShouldNotThrowValidationExceptionWhenUserBirthdayInPast() {
        // given
        User user = new User(0, "foo@bar.test", "foo", "bar", LocalDate.now().minusDays(1));

        // expect
        assertDoesNotThrow(() -> controller.isValidUser(user),
                "Исключение не должно выбрасываться когда день рождение в укаано в прошлом");
    }
}