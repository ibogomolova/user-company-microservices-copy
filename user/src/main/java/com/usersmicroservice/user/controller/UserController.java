package com.usersmicroservice.user.controller;

import com.usersmicroservice.user.dto.UserDto;
import com.usersmicroservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST-контроллер для управления пользователями.
 * Предоставляет CRUD-эндпоинты для работы с пользователями.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserDto create(@RequestBody UserDto dto) {
        return userService.create(dto);
    }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable UUID id, @RequestBody UserDto dto) {
        return userService.update(id, dto);
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable UUID id) {
        return userService.getById(id);
    }

    @GetMapping("/by-company/{companyId}")
    public List<UserDto> getUsersByCompany(@PathVariable UUID companyId) {
        return userService.getUsersByCompanyId(companyId);
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
