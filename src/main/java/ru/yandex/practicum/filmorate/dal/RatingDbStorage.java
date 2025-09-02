package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

@Component
public class RatingDbStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RatingDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Collection<Rating> findAll() {
        String sql = "SELECT r.* FROM rating r ORDER BY rating_id";

        return jdbcTemplate.query(sql, this::mapRowRating);
    }

    public Optional<Rating> getRatingById(int id) {
        String sql = "SELECT r.* FROM rating r WHERE r.rating_id = ?";

//        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::mapRowRating, id));

        Integer ratingId = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM rating WHERE rating_id = ?",
                Integer.class, id);

        System.out.println("ratingId bd " + ratingId);

        if (ratingId != 0) {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::mapRowRating, id));
        } else {
            throw new NotFoundException("Рейтинг с id = " + id + " не найден");
        }
    }

    private Rating mapRowRating(ResultSet resultSet, int rowNum) throws SQLException {
        Rating rating = new Rating();
        rating.setId(resultSet.getInt("rating_id"));
        rating.setName(resultSet.getString("name"));

        return rating;
    }
}