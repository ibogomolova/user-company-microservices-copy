package com.companymicroservice.company.validation;

import jakarta.validation.Constraint;
import org.springframework.messaging.handler.annotation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UserInfoValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUserInfo {
    String message() default "Некорректные данные пользователя";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
