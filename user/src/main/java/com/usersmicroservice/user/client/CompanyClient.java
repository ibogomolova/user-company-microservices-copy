package com.usersmicroservice.user.client;

import com.usersmicroservice.user.dto.CompanyInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign-клиент для взаимодействия с микросервисом компаний.
 * <p>
 * Используется в микросервисе пользователей для получения информации о компании по её ID.
 */
@FeignClient(name = "company-service", url = "${company.service.url}")
public interface CompanyClient {

    /**
     * Получает информацию о компании по её ID.
     *
     * @param id уникальный идентификатор компании
     * @return объект с данными о компании
     */
    @GetMapping("/companies/{id}")
    CompanyInfoDto getCompanyById(@PathVariable UUID id);
}
