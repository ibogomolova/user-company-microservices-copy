package com.companymicroservice.company.client;

import com.companymicroservice.company.dto.UserInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "user", path = "/users")
public interface UserClient {

    @GetMapping("/{id}")
    UserInfoDto getUserById(@PathVariable("id") UUID id);

    @GetMapping("/list")
    List<UserInfoDto> getAllUsers();
}
