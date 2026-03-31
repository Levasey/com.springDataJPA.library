package com.springdatajpa.library.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;
import java.time.ZoneId;

public class YearPublishedValidator implements ConstraintValidator<YearPublished, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        int max = Year.now(ZoneId.systemDefault()).getValue();
        return value >= 1500 && value <= max;
    }
}
