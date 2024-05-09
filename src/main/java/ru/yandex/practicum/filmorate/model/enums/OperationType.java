package ru.yandex.practicum.filmorate.model.enums;

import lombok.Getter;

@Getter
public enum OperationType {
    ADD(1),
    REMOVE(2),
    UPDATE(3);

    private Integer id;

    OperationType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
