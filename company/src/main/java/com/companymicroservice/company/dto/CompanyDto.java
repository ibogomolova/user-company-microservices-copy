package com.companymicroservice.company.dto;

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
    private String name;
    private BigDecimal budget;
    private List<UserInfoDto> users;
}
