package ru.yandex.practicum.filmorate.model.enums;

public enum OperationType {
    ADD(1),
    REMOVE(2),
    UPDATE(3);

    Integer id;

    OperationType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
