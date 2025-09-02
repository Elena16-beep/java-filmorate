package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.RatingDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final RatingDbStorage ratingDbStorage;
    private final GenreDbStorage genreDbStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       RatingDbStorage ratingDbStorage, GenreDbStorage genreDbStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.ratingDbStorage = ratingDbStorage;
        this.genreDbStorage = genreDbStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        ratingDbStorage.getRatingById(film.getRating().getId());
        genreDbStorage.validateGenre(film);

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        getById(film.getId());
        ratingDbStorage.getRatingById(film.getRating().getId());
        genreDbStorage.validateGenre(film);

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
        return filmStorage.getPopular(count);
    }

    public Film getById(Long id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }

    public void deleteById(Long id) {
        filmStorage.deleteById(id);
        log.info("Фильм {} удален", id);
    }
}