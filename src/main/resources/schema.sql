CREATE TABLE IF NOT EXISTS users (id int4 GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,name varchar,email varchar NOT NULL UNIQUE,login varchar NOT NULL UNIQUE,birthday date NOT NULL)

CREATE TABLE IF NOT EXISTS rating (id int4 GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,name varchar UNIQUE)

CREATE TABLE IF NOT EXISTS movies (id int4 GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,title varchar NOT NULL,description varchar(200) NOT NULL,duration int4 NOT NULL,release_date date NOT NULL,rating_id int4 REFERENCES rating(id) )

CREATE TABLE IF NOT EXISTS friends(user_id int4 REFERENCES users(id) ON DELETE CASCADE,friend_id int4 REFERENCES users(id) ON DELETE CASCADE,status boolean)

CREATE TABLE IF NOT EXISTS movie_like(movie_id int4 REFERENCES movies(id) ON DELETE CASCADE,user_id int4 REFERENCES users(id) ON DELETE CASCADE)

CREATE TABLE IF NOT EXISTS genre(id int4 GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,name varchar UNIQUE)

CREATE TABLE IF NOT EXISTS movie_genre(movie_id int4 NOT NULL REFERENCES movies(id) ON DELETE CASCADE,genre_id int4 NOT NULL REFERENCES genre(id) ON DELETE CASCADE)