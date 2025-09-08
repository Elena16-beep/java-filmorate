package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.Validation;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film create(Film film) {
        Validation.validateFilm(film, false);

        film.setId(getNextId());
        log.info("Фильм добавлен");
        films.put(film.getId(), film);

        return film;
    }

    public Film update(Film newFilm) {
        Validation.validateFilm(newFilm, true);

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setDuration(newFilm.getDuration());
            log.info("Фильм обновлен");

            return oldFilm;
        }

        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        getFilmById(filmId).ifPresentOrElse(
                film -> film.getLikes().add(userId),
                () -> {
                    throw new NotFoundException("Фильм с id =" + filmId + " не найден");
                }
        );
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        getFilmById(filmId).ifPresentOrElse(
                film -> film.getLikes().remove(userId),
                () -> {
                    throw new NotFoundException("Фильм с id =" + filmId + " не найден");
                }
        );
    }

    @Override
    public void deleteById(Long id) {
        getFilmById(id).ifPresentOrElse(
                film -> films.remove(id),
                () -> {
                    throw new NotFoundException("Фильм с id =" + id + " не найден");
                }
        );
    }

    @Override
    public List<Film> getPopular(int count) {
        return findAll()
                .stream()
                .sorted((film1, film2) -> Integer.compare(film2.getLikes().size(), film1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentMaxId;
    }
}