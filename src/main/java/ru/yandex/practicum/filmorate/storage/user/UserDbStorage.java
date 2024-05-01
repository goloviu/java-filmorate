package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component("userDbStorage")
@Slf4j
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User addUser(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        Integer dbUserId = simpleJdbcInsert.executeAndReturnKey(user.toMap()).intValue();
        user.setId(dbUserId);

        log.info("Пользователь добавлен в базу данных в таблицу users по ID: {} \n {}", dbUserId, user);
        return user;
    }

    @Override
    public User removeUser(User user) {
        String sqlDeleteUser = "DELETE FROM users WHERE id = ?";

        jdbcTemplate.update(sqlDeleteUser, user.getId());
        log.info("Пользователь по ID: {} и инфомрация о друзьях была удалена из базы данных в таблицах users, friends",
                user.getId());
        return user;
    }

    @Override
    public boolean removeFriendById(Integer userId, Integer friendId) {
        String sqlDeleteQuery = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        return jdbcTemplate.update(sqlDeleteQuery, userId, friendId) > 1;
    }

    @Override
    public User updateUser(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, login = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getName(), user.getEmail(), user.getLogin(), user.getBirthday(), user.getId());

        saveUserFriendsToDb(user);

        String sqlReadQuery = "SELECT * FROM users WHERE id = ?";
        User updatedUser = jdbcTemplate.queryForObject(sqlReadQuery, this::mapRowToUser, user.getId());
        log.info("Пользователь успешно обновлен в базе данных по таблице users. \n {}", updatedUser);
        return updatedUser;
    }

    @Override
    public User getUserById(Integer userId) {
        String sqlReadQuery = "SELECT * FROM users WHERE id = ?";
        User user = jdbcTemplate.queryForObject(sqlReadQuery, this::mapRowToUser, userId);

        log.info("Получен пользователь из базы данных по таблице users. \n {}", user);
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        String sqlReadQuery = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sqlReadQuery, this::mapRowToUser);
        log.info("Получены пользователи из базы данных по таблице users. {}", users);
        return users;
    }

    @Override
    public boolean isUserExist(Integer userId) {
        String sql = "SELECT COUNT(id) FROM users WHERE id = ?";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, userId);
        rs.next();
        return rs.getInt(1) > 0;
    }

    private void addToUserFriendsFromDb(User user) {
        Integer userId = user.getId();
        String sql = "SELECT * FROM friends WHERE user_id = ?";

        Set<Integer> friendsRequests = new HashSet<>();
        Set<Integer> friends = new HashSet<>();

        jdbcTemplate.query(sql, rs -> {
            if (rs.getBoolean("status")) {
                friends.add(rs.getInt("friend_id"));
            } else {
                friendsRequests.add(rs.getInt("friend_id"));
            }
        }, userId);

        user.setFriends(friends);
        user.setFriendsRequests(friendsRequests);
    }

    private void saveUserFriendsToDb(User user) {
        List<Integer> friendsRequests = new ArrayList<>(user.getFriendsRequests());
        List<Integer> friends = new ArrayList<>(user.getFriends());
        Integer userId = user.getId();

        String sql = "INSERT INTO friends (user_id, friend_id, status) " +
                "VALUES (?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Integer friendId = friendsRequests.get(i);
                ps.setInt(1, userId);
                ps.setInt(2, friendId);
                ps.setBoolean(3, false);
            }

            @Override
            public int getBatchSize() {
                return friendsRequests.size();
            }
        });

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Integer friendId = friends.get(i);
                ps.setInt(1, userId);
                ps.setInt(2, friendId);
                ps.setBoolean(3, true);
            }

            @Override
            public int getBatchSize() {
                return friends.size();
            }
        });
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = User.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
        addToUserFriendsFromDb(user);
        return user;
    }

    static Map mapLikesMap(ResultSet rs) throws SQLException {
        Map<Integer, List<Integer>> mapUserLikes = new HashMap<>();
        while (rs.next()) {
            if (mapUserLikes.containsKey(rs.getInt("user_id"))) {
                List<Integer> films = new ArrayList<>(mapUserLikes.get(rs.getInt("user_id")));
                films.add(rs.getInt("movie_id"));
                mapUserLikes.put(rs.getInt("user_id"), films);
            } else {
                mapUserLikes.put(rs.getInt("user_id"), List.of(rs.getInt("movie_id")));
            }
        }
        return mapUserLikes;
    }

    @Override
    public Map<Integer, List<Integer>> getLikes() {
        String sql = "SELECT * FROM movie_like ";
        Map<Integer, List<Integer>> likes = jdbcTemplate.query(sql, UserDbStorage::mapLikesMap);
        return likes;
    }
}
