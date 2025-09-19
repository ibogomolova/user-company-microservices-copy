package com.companymicroservice.company.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO для представления информации о пользователе,
 * связанного с компанией.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfoDto {

    private UUID id;

    @NotBlank(message = "Имя пользователя обязательно")
    @Size(max = 50, message = "Имя пользователя не должно превышать 50 символов")
    private String firstName;

    @NotBlank(message = "Фамилия пользователя обязательна")
    @Size(max = 255, message = "Фамилия пользователя не должна превышать 50 символов")
    private String lastName;

    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "^\\+\\d{10,15}$", message = "Телефон должен быть в формате +3737777890")
    private String phone;
}
