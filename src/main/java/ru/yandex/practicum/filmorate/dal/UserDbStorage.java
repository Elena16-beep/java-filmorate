package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validation.Validation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT u.* FROM users u";

        return jdbcTemplate.query(sql, this::mapRowUser);
    }

    @Override
    public User create(User user) {
        user.setId(getNextId());
        String sql = "INSERT INTO users (user_id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                user.getId(),
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());

        return user;
    }

    @Override
    public User update(User user) {
        Validation.validateUser(user);

        User newUser = getUserById(user.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + user.getId() + " не найден"));

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }

        return user;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        String sql = "SELECT u.* FROM users u WHERE u.user_id = ?";

        Long userId = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE user_id = ?",
                Long.class, id);

        if (userId == 0) {
            return Optional.empty();
        }

        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::mapRowUser, id));
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        String sql = "INSERT INTO friendship (user_id, friend_id, isConfirmed) VALUES (?, ?, true)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long id) {
        String sql = "SELECT u.* FROM users u INNER JOIN friendship f on u.user_id = f.friend_id WHERE f.user_id = ?";

        return jdbcTemplate.query(sql, this::mapRowUser, id);
    }

    @Override
    public Collection<User> getCommonFriends(Long id) {
        return jdbcTemplate.query("SELECT u.user_id, name, email, login, birthday " +
                        "FROM users u JOIN friendship f on u.user_id = f.friend_id WHERE f.user_id = ?",
                this::mapRowUser, id);
    }

    private User mapRowUser(ResultSet resultSet, int rowNum) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("user_id"));
        user.setEmail(resultSet.getString("email"));
        user.setLogin(resultSet.getString("login"));
        user.setName(resultSet.getString("name"));
        user.setBirthday(resultSet.getDate("birthday").toLocalDate());

        return user;
    }

    private Long getNextId() {
        String sql = "SELECT COALESCE(MAX(user_id), 0) FROM users";
        Long id = jdbcTemplate.queryForObject(sql, Long.class);

        return (id != null) ? id + 1 : 0;
    }
}