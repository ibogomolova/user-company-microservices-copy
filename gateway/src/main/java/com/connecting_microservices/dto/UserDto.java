package com.connecting_microservices.dto;

import java.util.UUID;

/**
 * DTO пользователя, получаемого из сервиса пользователей.
 */
public record UserDto(UUID id, String firstName, String lastName, String phone, UUID companyId) {
}
