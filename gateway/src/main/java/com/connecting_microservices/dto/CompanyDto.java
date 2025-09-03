package com.connecting_microservices.dto;

import java.util.UUID;

/**
 * DTO компании, получаемой из сервиса компаний.
 */
public record CompanyDto(UUID id, String name, double budget) {
}
