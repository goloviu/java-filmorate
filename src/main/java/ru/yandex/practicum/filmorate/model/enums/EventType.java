package ru.yandex.practicum.filmorate.model.enums;

public enum EventType {
    LIKE(1),
    REVIEW(2),
    FRIEND(3);

    Integer id;

    EventType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
