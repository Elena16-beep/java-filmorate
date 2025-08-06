package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class UserControllerTest {
    UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void createUser() {
        User user = new User();
        user.setEmail("email1@b.c");
        user.setLogin("Login1");
        user.setName("Test1");
        user.setBirthday(LocalDate.now());

        assertDoesNotThrow(() -> userController.create(user));
        assertEquals(1, userController.findAll().size());
    }

    @Test
    void createUserWithEmailIsNull() {
        User user = new User();
        user.setEmail("");
        user.setLogin("Login2");
        user.setName("Test2");
        user.setBirthday(LocalDate.now());

        assertThrows(ValidationException.class,
                () -> userController.create(user),
                "Емайл не может быть пустым");
        assertEquals(0, userController.findAll().size());
    }

    @Test
    void createUserWithEmailIsNotValid() {
        User user = new User();
        user.setEmail("email3.b.c");
        user.setLogin("Login3");
        user.setName("Test3");
        user.setBirthday(LocalDate.now());

        assertThrows(ValidationException.class,
                () -> userController.create(user),
                "Емайл должен содержать @");
        assertEquals(0, userController.findAll().size());
    }

    @Test
    void createUserWithLoginIsNull() {
        User user = new User();
        user.setEmail("email4@b.c");
        user.setLogin("");
        user.setName("Test4");
        user.setBirthday(LocalDate.now());

        assertThrows(ValidationException.class,
                () -> userController.create(user),
                "Логин не может быть пустым");
        assertEquals(0, userController.findAll().size());
    }

    @Test
    void createUserWithLoginIsNotValid() {
        User user = new User();
        user.setEmail("email5@b.c");
        user.setLogin("Login 5");
        user.setName("Test5");
        user.setBirthday(LocalDate.now());

        assertThrows(ValidationException.class,
                () -> userController.create(user),
                "Логин не может содержать пробелы");
        assertEquals(0, userController.findAll().size());
    }

    @Test
    void createUserWithBirthdayIsInFuture() {
        User user = new User();
        user.setEmail("email6@b.c");
        user.setLogin("Login6");
        user.setName("Test6");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class,
                () -> userController.create(user),
                "Дата рождения не может быть в будущем");
        assertEquals(0, userController.findAll().size());
    }

    @Test
    void createUserWithNameIsNull() {
        User user = new User();
        user.setEmail("email7@b.c");
        user.setLogin("Login7");
        user.setName(null);
        user.setBirthday(LocalDate.now());
        userController.create(user);

        assertEquals(user.getLogin(), user.getName());
        assertEquals(1, userController.findAll().size());
    }

    @Test
    void updateUser() {
        User user1 = new User();
        user1.setEmail("email8@b.c");
        user1.setLogin("Login8");
        user1.setName("Test8");
        user1.setBirthday(LocalDate.now());

        Film film1 = new Film();
        film1.setName("Test6");
        film1.setDescription("f".repeat(200));
        film1.setReleaseDate(LocalDate.of(2025, 8, 1));
        film1.setDuration(100);

        User user2 = userController.create(user1);
        user2.setEmail("email9@b.c");
        user2.setLogin("Login9");
        user2.setName("Test9");
        User user3 = userController.update(user2);

        assertEquals("email9@b.c", user3.getEmail());
        assertEquals("Login9", user3.getLogin());
        assertEquals("Test9", user3.getName());
        assertEquals(1, userController.findAll().size());
    }

    @Test
    void updateUserWithIdNotFound() {
        User user = new User();
        user.setEmail("email10@b.c");
        user.setLogin("Login10");
        user.setName("Test10");
        user.setBirthday(LocalDate.now());
        user.setId(10L);

        assertThrows(NotFoundException.class,
                () -> userController.update(user),
                "Пользователь с id = 10 не найден");
    }

    @Test
    void findAllUsers() {
        User user1 = new User();
        user1.setEmail("email11@b.c");
        user1.setLogin("Login11");
        user1.setName("Test11");
        user1.setBirthday(LocalDate.of(2020, 1, 1));

        User user2 = new User();
        user2.setEmail("email12@b.c");
        user2.setLogin("Login12");
        user2.setName("Test12");
        user2.setBirthday(LocalDate.of(2000, 2, 2));

        userController.create(user1);
        userController.create(user2);

        assertEquals(2, userController.findAll().size());
    }
}
