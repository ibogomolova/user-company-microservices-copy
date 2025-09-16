package com.companymicroservice.company.client;

import com.companymicroservice.company.dto.UserInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

/**
 * Feign-клиент для взаимодействия с микросервисом пользователей.
 * <p>
 * Используется в микросервисе компаний для получения списка сотрудников по ID компании.
 */
@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserClient {

    /**
     * Получает список пользователей по ID компании.
     *
     * @param companyId уникальный идентификатор компании
     * @return список пользователей, работающих в компании
     */
    @GetMapping("/users/by-company/{companyId}")
    List<UserInfoDto> getUsersByCompany(@PathVariable UUID companyId);

    @GetMapping("/users/{id}")
    UserInfoDto getUserById(@PathVariable UUID id);
}
