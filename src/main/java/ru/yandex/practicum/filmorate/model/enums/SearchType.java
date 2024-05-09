package ru.yandex.practicum.filmorate.model.enums;

import lombok.Getter;

@Getter
public enum SearchType {
    DIRECTOR("director"),
    TITLE("title");
    private String label;

    SearchType(String label) {
        this.label = label;
    }

    public static SearchType valueOfLabel(String label) {
        for (SearchType e : values()) {
            if (e.label.equals(label)) {
                return e;
            }
        }
        return null;
    }
}
