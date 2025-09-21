package com.usersmicroservice.user.dto;

import lombok.Data;

import java.util.UUID;

/**
 * DTO (Data Transfer Object) для юзера.
 * Используется для передачи данных между слоями приложения.
 */
@Data
public class UserDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String phone;
    private CompanyInfoDto company;
}
