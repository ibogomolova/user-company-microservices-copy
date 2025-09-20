package com.companymicroservice.company.validation;

import com.companymicroservice.company.dto.UserInfoDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;


public class UserInfoValidator implements ConstraintValidator<ValidUserInfo, UserInfoDto> {

    private static final Pattern PHONE_REGEX = Pattern.compile("^\\+\\d{10,15}$");
    private static final Pattern NAME_REGEX = Pattern.compile("^[A-ZА-Я][a-zа-я]*$");
    private static final int MAX_NAME_LEN = 50;

    @Override
    public boolean isValid(UserInfoDto user, ConstraintValidatorContext context) {
        if (user == null) {
            return true;
        }

        String firstName = trimToNull(user.getFirstName());
        String lastName = trimToNull(user.getLastName());
        String phone = trimToNull(user.getPhone());

        if (user.getId() != null) {
            boolean hasExtra = firstName != null || lastName != null || phone != null;
            if (hasExtra) {

                if (firstName != null) addFieldViolation(context, "firstName", "Нельзя передавать firstName, если указан id");
                if (lastName != null) addFieldViolation(context, "lastName", "Нельзя передавать lastName, если указан id");
                if (phone != null) addFieldViolation(context, "phone", "Нельзя передавать phone, если указан id");
                return false;
            }
            return true;
        }

        boolean valid = true;

        if (firstName == null || firstName.isBlank()) {
            addFieldViolation(context, "firstName", "Имя пользователя обязательно");
            valid = false;
        } else if (firstName.length() > MAX_NAME_LEN) {
            addFieldViolation(context, "firstName", "Имя пользователя не должно превышать " + MAX_NAME_LEN + " символов");
            valid = false;
        } else if (!NAME_REGEX.matcher(firstName).matches()) {
            addFieldViolation(context, "firstName", "Имя пользователя должно начинаться с большой буквы и содержать только буквы");
            valid = false;
        }

        if (lastName == null || lastName.isBlank()) {
            addFieldViolation(context, "lastName", "Фамилия пользователя обязательна");
            valid = false;
        } else if (lastName.length() > MAX_NAME_LEN) {
            addFieldViolation(context, "lastName", "Фамилия пользователя не должна превышать " + MAX_NAME_LEN + " символов");
            valid = false;
        } else if (!NAME_REGEX.matcher(lastName).matches()) {
            addFieldViolation(context, "lastName", "Фамилия пользователя должна начинаться с большой буквы и содержать только буквы");
            valid = false;
        }

        if (phone == null || phone.isBlank()) {
            addFieldViolation(context, "phone", "Телефон обязателен");
            valid = false;
        } else if (!PHONE_REGEX.matcher(phone).matches()) {
            addFieldViolation(context, "phone", "Телефон должен быть в формате +3737777890 и содержать 10-15 цифр");
            valid = false;
        }

        return valid;
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private void addFieldViolation(ConstraintValidatorContext context, String field, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}
