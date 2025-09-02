package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.RatingDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;
import java.util.Collection;

@Slf4j
@Service
public class RatingService {
    private final RatingDbStorage ratingDbStorage;

    @Autowired
    public RatingService(RatingDbStorage ratingDbStorage) {
        this.ratingDbStorage = ratingDbStorage;
    }

    public Collection<Rating> findAll() {
        return ratingDbStorage.findAll();
    }

    public Rating getById(int id) {
        return ratingDbStorage.getRatingById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id = " + id + " не найден"));
    }
}