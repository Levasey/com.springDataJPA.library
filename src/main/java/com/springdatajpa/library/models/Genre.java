package com.springdatajpa.library.models;

public enum Genre {
    FICTION("Художественная проза"),
    NON_FICTION("Документалистика / научпоп"),
    SCIENCE_FICTION("Фантастика"),
    FANTASY("Фэнтези"),
    DETECTIVE("Детектив"),
    POETRY("Поэзия"),
    DRAMA("Драматургия"),
    CHILDREN("Детская литература"),
    EDUCATIONAL("Учебная литература"),
    BIOGRAPHY("Биография / мемуары"),
    HISTORY("История"),
    PHILOSOPHY("Философия"),
    OTHER("Другое");

    private final String displayName;

    Genre(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
