package com.usersmicroservice.user.service.impl;

import com.usersmicroservice.user.client.CompanyClient;
import com.usersmicroservice.user.dto.CompanyInfoDto;
import com.usersmicroservice.user.dto.UserDto;
import com.usersmicroservice.user.entity.User;
import com.usersmicroservice.user.exception.UserNotFoundException;
import com.usersmicroservice.user.mapper.UserMapper;
import com.usersmicroservice.user.reposirory.UserRepository;
import com.usersmicroservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CompanyClient companyClient;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        User saved = userRepository.save(user);

        CompanyInfoDto company = null;
        if (saved.getCompanyId() != null) {
            company = companyClient.getCompanyById(saved.getCompanyId());
        }

        return userMapper.toDto(saved, company);
    }

    @Override
    @Transactional
    public UserDto update(UUID id, UserDto userDto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));

        existing.setFirstName(userDto.getFirstName());
        existing.setLastName(userDto.getLastName());
        existing.setPhone(userDto.getPhone());
        if (userDto.getCompany() != null) {
            existing.setCompanyId(userDto.getCompany().getId());
        }

        User updated = userRepository.save(existing);

        CompanyInfoDto company = null;
        if (updated.getCompanyId() != null) {
            company = companyClient.getCompanyById(updated.getCompanyId());
        }

        return userMapper.toDto(updated, company);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));

        CompanyInfoDto company = null;
        if (user.getCompanyId() != null) {
            company = companyClient.getCompanyById(user.getCompanyId());
        }

        return userMapper.toDto(user, company);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        List<User> users = userRepository.findAll();
        List<UserDto> dtos = new ArrayList<>();
        for (User u : users) {
            CompanyInfoDto company = null;
            if (u.getCompanyId() != null) {
                company = companyClient.getCompanyById(u.getCompanyId());
            }
            dtos.add(userMapper.toDto(u, company));
        }
        return dtos;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        userRepository.deleteById(id);
    }
}
