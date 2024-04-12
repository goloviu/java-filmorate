java-filmorate  
---
Прототип социальной сети, на основе RESTful API, позволяющее пользователям оценивать фильмы
и делиться впечатлением о них, добавляя друг-друга в друзья.

## Стек
* Java 11
* Spring Boot
* Maven
* H2
---
## Схема базы данных и примеры запросов

![Data Base Diagram](https://github.com/goloviu/java-filmorate/assets/147878926/58895020-8341-4dbe-8ba1-a2a549415f57)

#### User
- Получение всех пользователей
```roomsql
SELECT *
FROM user;
```
- Получение пользователя по ID
```roomsql
SELECT *
FROM user
WHERE user_id = <id>;
```
- Получение общих друзей 
```roomsql
SELECT *
FROM user
WHERE user_id IN (
    SELECT friend_id FROM (
        SELECT friend_id FROM friends WHERE user_id = <user-id>    
        )
    WHERE friend_id IN (
        SELECT friend_id FROM friends WHERE user_id = <other-user-id>
        )
)   
```
- Получение друзей пользователя по ID
```roomsql
SELECT *
FROM user AS u
WHERE u.user_id = <id>
INNER JOIN friends AS f ON f.user_id = u.user_id;
```
#### movie
- Получение всех фильмов
```roomsql
SELECT *
FROM movie;
```
- Получение фильма по ID
```roomsql
SELECT *
FROM movie
WHERE movie_id = <id>
```
