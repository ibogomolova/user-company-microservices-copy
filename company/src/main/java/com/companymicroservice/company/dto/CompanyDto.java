package com.companymicroservice.company.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) для компании.
 * Используется для передачи данных между слоями приложения.
 */
@Data
public class CompanyDto {

    private UUID id;

    @NotBlank(message = "Название компании обязательно")
    @Size(max = 50, message = "Название компании не должно превышать 50 символов")
    private String name;

    @NotNull(message = "Бюджет компании обязателен")
    @DecimalMin(value = "0.0", inclusive = false, message = "Бюджет должен быть больше 0")
    private BigDecimal budget;

    @Valid
    private List<UserInfoDto> users;
}
