package com.connecting_microservices.dto;

import java.util.UUID;

public record UserDto(UUID id, String firstName, String lastName, String phone, UUID companyId) {
}
