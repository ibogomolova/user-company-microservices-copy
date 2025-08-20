package com.usersmicroservice.user.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CompanyInfoDto {
    private UUID id;
    private String name;
}
