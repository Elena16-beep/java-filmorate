package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("Емайл не может быть пустым");
            throw new ValidationException("Емайл не может быть пустым");
        }

        if (!user.getEmail().contains("@")) {
            log.error("Емайл {} должен содержать @", user.getEmail());
            throw new ValidationException("Емайл должен содержать @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() ||
                user.getLogin().contains(" ")) {
            log.error("Логин {} не может быть пустым и содержать пробелы", user.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения {} не может быть в будущем", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        log.info("Пользователь добавлен");
        users.put(user.getId(), user);

        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.error("Id не может быть пустым");
            throw new ValidationException("Id должен быть указан");
        }

        if (newUser.getEmail() == null || newUser.getEmail().isBlank()) {
            log.error("Емайл не может быть пустым");
            throw new ValidationException("Емайл не может быть пустым");
        }

        if (!newUser.getEmail().contains("@")) {
            log.error("Емайл {} должен содержать @", newUser.getEmail());
            throw new ValidationException("Емайл должен содержать @");
        }

        if (newUser.getLogin() == null || newUser.getLogin().isBlank() ||
                newUser.getLogin().contains(" ")) {
            log.error("Логин {} не может быть пустым и содержать пробелы", newUser.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }

        if (newUser.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения {} не может быть в будущем", newUser.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            if (newUser.getName() == null || newUser.getName().isBlank()) {
                oldUser.setName(newUser.getLogin());
            } else {
                oldUser.setName(newUser.getName());
            }

            oldUser.setEmail(newUser.getEmail());
            oldUser.setLogin(newUser.getLogin());
            oldUser.setBirthday(newUser.getBirthday());
            log.info("Пользователь обновлен");

            return oldUser;
        }

        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentMaxId;
    }
}
