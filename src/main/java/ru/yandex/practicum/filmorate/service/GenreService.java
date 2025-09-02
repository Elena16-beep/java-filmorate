package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class GenreService {
    private final GenreDbStorage genreDbStorage;

    @Autowired
    public GenreService(GenreDbStorage genreDbStorage) {
        this.genreDbStorage = genreDbStorage;
    }

    public Collection<Genre> findAll() {
        return genreDbStorage.findAll();
    }

    public Genre getById(int id) {
        return genreDbStorage.getGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден"));
    }

    public List<Genre> getGenresByFilmId(Long filmId) {
        return genreDbStorage.getGenresByFilmId(filmId);
    }
}