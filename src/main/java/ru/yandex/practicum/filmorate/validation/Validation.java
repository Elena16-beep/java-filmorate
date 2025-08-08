package ru.yandex.practicum.filmorate.validation;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Slf4j
public class Validation {
    public static void validateFilm(Film film, boolean validateId) {
        if (validateId && film.getId() == null) {
            log.error("Id фильма не может быть пустым");
            throw new ValidationException("Id фильма должен быть указан");
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Дата релиза {} раньше 28 декабря 1895 года", film.getReleaseDate());
            throw new ValidationException("Дата релиза не раньше 28 декабря 1895 года");
        }
    }

    public static void validateUser(User user) {
        if (user.getId() == null) {
            log.error("Id пользователя не может быть пустым");
            throw new ValidationException("Id пользователя должен быть указан");
        }
    }
}
