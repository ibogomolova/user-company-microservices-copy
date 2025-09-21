package com.usersmicroservice.user.service;

import com.usersmicroservice.user.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto update(UUID id, UserDto userDto);

    UserDto getById(UUID id);

    List<UserDto> getUsersByCompanyId(UUID companyId);

    Page<UserDto> getAll(Pageable pageable);

    void delete(UUID id);

    void syncUserFromCompany(UUID userId, String firstName, String lastName, String phone, UUID companyId);

    void deleteUsersByCompanyId(UUID companyId);
}
