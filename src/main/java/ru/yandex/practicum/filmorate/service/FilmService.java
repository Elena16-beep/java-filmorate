package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        userStorage.getUserById(userId).ifPresentOrElse(
                user -> filmStorage.addLike(filmId, userId),
                () -> {
                    throw new NotFoundException("Пользователь с id =" + userId + " не найден");
                }
        );
    }

    public void deleteLike(Long filmId, Long userId) {
        userStorage.getUserById(userId).ifPresentOrElse(
                user -> filmStorage.deleteLike(filmId, userId),
                () -> {
                    throw new NotFoundException("Пользователь с id =" + userId + " не найден");
                }
        );
    }

    public List<Film> getPopular(int count) {
        return filmStorage.findAll()
                .stream()
                .sorted((film1, film2) -> Integer.compare(film2.getLikes().size(), film1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}