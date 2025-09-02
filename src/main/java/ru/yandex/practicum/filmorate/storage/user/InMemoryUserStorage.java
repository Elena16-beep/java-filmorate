package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.Validation;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        log.info("Пользователь добавлен");
        users.put(user.getId(), user);

        return user;
    }

    @Override
    public User update(User newUser) {
        Validation.validateUser(newUser);

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

    @Override
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void deleteById(Long id) {
        getUserById(id).ifPresentOrElse(
                user -> users.remove(id),
                () -> {
                    throw new NotFoundException("Пользователь с id =" + id + " не найден");
                }
        );
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
    }

    @Override
    public List<User> getFriends(Long id) {
        User user = getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));

        return user.getFriends().stream()
                .map(this::getUserById)
                .map(opt -> opt.orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

//    @Override
//    public List<Long> getCommonFriends(Long userId, Long otherId) {
//        User user = getUserById(userId)
//                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
//
//        User otherUser = getUserById(otherId)
//                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + otherId + " не найден"));
//
//        return user.getFriends().stream()
//                .filter(friendId -> otherUser.getFriends().contains(friendId))
////                .map(this::getUserById)
////                .map(opt -> opt.orElse(null))
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//    }

public Collection<User> getCommonFriends(Long id) {
    return null;
}

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentMaxId;
    }
}
