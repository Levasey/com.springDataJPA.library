package com.springdatajpa.library.exception;

/**
 * Бизнес-конфликт (409). Необязательное {@code field} — имя поля формы для {@code BindingResult.rejectValue}.
 */
public class ConflictException extends RuntimeException {

    private final String field;

    public ConflictException(String message) {
        this(message, null);
    }

    public ConflictException(String message, String field) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
