package com.companymicroservice.company.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserInfoDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String phone;
}
