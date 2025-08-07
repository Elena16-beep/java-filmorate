package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class FilmControllerTest {
    FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
    }

    @Test
    void createFilm() {
        Film film = new Film();
        film.setName("Test1");
        film.setDescription("a".repeat(200));
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(200);

        assertDoesNotThrow(() -> filmController.create(film));
        assertEquals(1, filmController.findAll().size());
    }

    @Test
    void createFilmWithReleaseDateIsBefore1895() {
        Film film = new Film();
        film.setName("Test4");
        film.setDescription("d".repeat(200));
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(1);

        assertThrows(ValidationException.class,
                () -> filmController.create(film),
                "Дата релиза не раньше 28 декабря 1895 года");
        assertEquals(0, filmController.findAll().size());
    }

    @Test
    void updateFilm() {
        Film film1 = new Film();
        film1.setName("Test6");
        film1.setDescription("f".repeat(200));
        film1.setReleaseDate(LocalDate.of(2025, 8, 1));
        film1.setDuration(100);

        Film film2 = filmController.create(film1);
        film2.setName("Test7");
        Film film3 = filmController.update(film2);

        assertEquals("Test7", film3.getName());
        assertEquals(1, filmController.findAll().size());
    }

    @Test
    void updateFilmWithIdNotFound() {
        Film film = new Film();
        film.setName("Test8");
        film.setDescription("g".repeat(200));
        film.setReleaseDate(LocalDate.of(2025, 8, 2));
        film.setDuration(110);
        film.setId(70L);

        assertThrows(NotFoundException.class,
                () -> filmController.update(film),
                "Фильм с id = 70 не найден");
    }

    @Test
    void updateFilmWithIdIsNull() {
        Film film = new Film();
        film.setName("Test11");
        film.setDescription("k".repeat(200));
        film.setReleaseDate(LocalDate.of(2025, 8, 4));
        film.setDuration(140);
        film.setId(null);

        assertThrows(ValidationException.class,
                () -> filmController.update(film),
                "Id не может быть пустым");
    }

    @Test
    void findAllFilms() {
        Film film1 = new Film();
        film1.setName("Test9");
        film1.setDescription("h".repeat(200));
        film1.setReleaseDate(LocalDate.of(2025, 8, 3));
        film1.setDuration(120);

        Film film2 = new Film();
        film2.setName("Test10");
        film2.setDescription("i".repeat(200));
        film2.setReleaseDate(LocalDate.of(2025, 8, 4));
        film2.setDuration(130);

        filmController.create(film1);
        filmController.create(film2);
        assertEquals(2, filmController.findAll().size());
    }
}
