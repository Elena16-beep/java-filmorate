package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class FilmorateApplicationTests {
    FilmController filmController;
    UserController userController;
    FilmStorage filmStorage = new InMemoryFilmStorage();
    UserStorage userStorage = new InMemoryUserStorage();

    @BeforeEach
    void setUp() {
        filmController = new FilmController(
                new FilmService(
                        filmStorage,
                        userStorage
                )
        );

        userController = new UserController(
                new UserService(userStorage)
        );
    }

	@Test
	void contextLoads() {
        assertNotNull(filmController, "Контроллер для фильмов не найден");
        assertNotNull(userController, "Контроллер для пользователей не найден");
	}
}
