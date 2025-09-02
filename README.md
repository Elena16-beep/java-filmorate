# Проект java-filmorate
## Рейтинг фильмов  
Бэкенд для сервиса, который будет работать с фильмами и оценками пользователей, а также возвращать топ-10 фильмов, рекомендованных к просмотру.<br>
Программа может принимать, обновлять и возвращать пользователей и фильмы.<br>
Вся информация будет храниться в базе данных. 

### Схема БД

![Схема БД](\src\main\resources\DB.png)

### Примеры запросов

SELECT *<br>
FROM film

SELECT *<br>
FROM user<br>
WHERE user_id = 2

SELECT film_id<br>
FROM like<br>
GROUP BY film_id<br>
ORDER BY COUNT(user_id) DESC<br>
LIMIT 10