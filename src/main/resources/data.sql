MERGE INTO rating KEY (name)
VALUES (1, 'G'), (2, 'PG'), (3, 'PG-13'), (4, 'R'), (5, 'NC-17');

MERGE INTO genre KEY (name)
VALUES (1, 'Комедия'), (2, 'Драма'), (3, 'Мультфильм'), (4, 'Триллер'), (5, 'Документальный'), (6, 'Боевик');

MERGE INTO event_types KEY (name)
VALUES (1, 'LIKE'), (2, 'REVIEW'), (3, 'FRIEND');

MERGE INTO operation KEY (name)
VALUES (1, 'ADD'), (2, 'REMOVE'), (3, 'UPDATE');