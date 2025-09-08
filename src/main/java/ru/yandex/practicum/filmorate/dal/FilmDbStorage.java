package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.RatingService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.validation.Validation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreService genreService;
    private final RatingService ratingService;

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT f.*, r.name rating_name FROM film f INNER JOIN rating r ON f.rating_id = r.rating_id";

        return jdbcTemplate.query(sql, this::mapRowFilm);
    }

    @Override
    public Film create(Film film) {
        Validation.validateFilm(film, false);
        film.setId(getNextId());

        String sql = """
                INSERT INTO film (film_id, name, description, release_date, duration, rating_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql,
                film.getId(),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRating().getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String genreSql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            film.getGenres().forEach(genre -> {
                jdbcTemplate.update(genreSql, film.getId(), genre.getId());
            });
        }

        film.setRating(ratingService.getById(film.getRating().getId()));

        return film;
    }

    @Override
    public Film update(Film film) {
        Validation.validateFilm(film, true);

        Film newFilm = getFilmById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + film.getId() + " не найден"));

        String sql = """
                UPDATE film SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ?
                WHERE film_id = ?
                """;

        int rowsUpdated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRating().getId(),
                film.getId());

        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String genreSql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";

            film.getGenres().forEach(genre -> {
                jdbcTemplate.update(genreSql, film.getId(), genre.getId());
            });

            film.getGenres().clear();
        }

        film.getGenres().addAll(genreService.getGenresByFilmId(film.getId()));
        film.setRating(ratingService.getById(film.getRating().getId()));

        return film;
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        String sql = """
                SELECT f.*, r.name rating_name FROM film f INNER JOIN rating r ON f.rating_id = r.rating_id
                WHERE f.film_id = ?
                """;

        Long filmId = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM film WHERE film_id = ?",
                Long.class, id);

        if (filmId == 0) {
            return Optional.empty();
        }

        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::mapRowFilm, id));
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public Map<Long, Set<Long>> getLikesForFilms(Collection<Long> filmIds) {
        String sql = String.format(
                "SELECT film_id, user_id FROM likes WHERE film_id IN (%s)",
                String.join(",", Collections.nCopies(filmIds.size(), "?"))
        );

        Map<Long, Set<Long>> likesMap = new HashMap<>();
        jdbcTemplate.query(sql, filmIds.toArray(), resultSet -> {
            Long filmId = resultSet.getLong("film_id");
            Long userId = resultSet.getLong("user_id");
            likesMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        });

        return likesMap;
    }

    public Set<Long> getLikesByFilmId(Long id) {
        String sql = "SELECT l.user_id FROM likes l WHERE l.film_id = ? ORDER BY film_id";

        return new HashSet<>(jdbcTemplate.query(sql,
                (resultSet, rowNum) -> resultSet.getLong("user_id"),
                id));
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM film WHERE film_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Film> getPopular(int count) {
        String sql = """
                SELECT f.*, r.name rating_name FROM film f
                INNER JOIN rating r ON f.rating_id = r.rating_id
                LEFT JOIN likes l ON f.film_id = l.film_id
                GROUP BY f.film_id
                ORDER BY COUNT(l.user_id) DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, this::mapRowFilm, count);
    }

    private Film mapRowFilm(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("film_id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        film.setDuration(resultSet.getInt("duration"));

        Rating rating = new Rating();
        rating.setId(resultSet.getInt("rating_id"));
        rating.setName(resultSet.getString("rating_name"));
        film.setRating(rating);
        film.getGenres().addAll(genreService.getGenresByFilmId(film.getId()));
        film.setLikes(getLikesByFilmId(film.getId()));

        return film;
    }

    private Long getNextId() {
        String sql = "SELECT COALESCE(MAX(film_id), 0) FROM film";
        Long id = jdbcTemplate.queryForObject(sql, Long.class);

        return id != null ? id + 1 : 0;
    }
}