package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
//        User newUser = userStorage.getUserById(user.getId())
//                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + user.getId() + " не найден"));

        return userStorage.update(user);
    }

    public void addFriend(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        User friend = userStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + friendId + " не найден"));

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        userStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        User friend = userStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + friendId + " не найден"));

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        userStorage.deleteFriend(userId, friendId);
    }

    public List<User> getFriends(Long id) {
        User user = userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));

        return userStorage.getFriends(id);
    }

    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        var friendsList = userStorage.getCommonFriends(friendId);
        return userStorage.getCommonFriends(userId).stream()
                .filter(friendsList::contains)
                .collect(Collectors.toList());
    }

    public User getById(Long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    public void deleteById(Long id) {
        userStorage.deleteById(id);
        log.info("Пользователь {} удален", id);
    }
}
