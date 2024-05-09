package ru.yandex.practicum.filmorate.model.enums;

import lombok.Getter;

@Getter
public enum EventType {
    LIKE(1),
    REVIEW(2),
    FRIEND(3);

    private Integer id;

    EventType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
