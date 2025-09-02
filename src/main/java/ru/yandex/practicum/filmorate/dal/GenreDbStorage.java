package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Collection<Genre> findAll() {
        String sql = "SELECT g.* FROM genre g ORDER BY genre_id";

        return jdbcTemplate.query(sql, this::mapRowGenre);
    }

    public Optional<Genre> getGenreById(int id) {
        String sql = "SELECT g.* FROM genre g WHERE g.genre_id = ?";

        Integer genreId = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM genre WHERE genre_id = ?",
                Integer.class, id);

        System.out.println("ratingId bd " + genreId);

        if (genreId != 0) {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::mapRowGenre, id));
        } else {
            throw new NotFoundException("Жанр с id = " + id + " не найден");
        }

//        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::mapRowGenre, id));
    }

    public void validateGenre(Film film) {
        for (Genre genre : film.getGenres()) {
            Integer genreId = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM genre WHERE genre_id = ?",
                    Integer.class, genre.getId());

            if (genreId == 0) {
                throw new NotFoundException("Жанр с id = " + genre.getId() + " не найден");
            }
        }
    }

    public List<Genre> getGenresByFilmId(Long id) {
//        String query = "select g.id, g.name from genres g"
//                + " inner join film_genres fg on fg.genre_id = g.id"
//                + " where fg.film_id = ?";
//        return getRecords(query, id);

        String sql = "SELECT g.* FROM genre g " +
                "INNER JOIN film_genre fg ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ? ORDER BY genre_id";

        return jdbcTemplate.query(sql, this::mapRowGenre, id);
    }

    private Genre mapRowGenre(ResultSet resultSet, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(resultSet.getInt("genre_id"));
        genre.setName(resultSet.getString("name"));

        return genre;
    }
}