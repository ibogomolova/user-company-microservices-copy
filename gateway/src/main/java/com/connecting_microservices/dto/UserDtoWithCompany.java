package com.connecting_microservices.dto;

/**
 * DTO для объединения данных пользователя и компании.
 * <p>
 * Используется в {@link com.connecting_microservices.controller.AggregationController} для возврата агрегированных данных.
 *
 * @param user    объект пользователя
 * @param company объект компании
 */
public record UserDtoWithCompany(UserDto user, CompanyDto company) {
}
