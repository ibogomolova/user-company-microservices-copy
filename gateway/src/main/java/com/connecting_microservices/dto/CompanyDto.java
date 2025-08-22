package com.connecting_microservices.dto;

import java.util.UUID;

public record CompanyDto(UUID id, String name, double budget) {
}
