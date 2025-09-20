package com.companymicroservice.company.dto;

import com.companymicroservice.company.validation.ValidUserInfo;
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
@ValidUserInfo
public class UserInfoDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String phone;
}
