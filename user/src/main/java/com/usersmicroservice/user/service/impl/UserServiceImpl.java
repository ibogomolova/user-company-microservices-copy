package com.usersmicroservice.user.service.impl;

import com.usersmicroservice.user.dto.UserDto;
import com.usersmicroservice.user.entity.User;
import com.usersmicroservice.user.exception.UserNotFoundException;
import com.usersmicroservice.user.mapper.UserMapper;
import com.usersmicroservice.user.reposirory.UserRepository;
import com.usersmicroservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        User saved = userRepository.save(user);
        return userMapper.toDto(saved, null);
    }

    @Override
    @Transactional
    public UserDto update(UUID id, UserDto userDto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));

        existing.setFirstName(userDto.getFirstName());
        existing.setLastName(userDto.getLastName());
        existing.setPhone(userDto.getPhone());
        existing.setCompanyId(userDto.getCompany() != null ? userDto.getCompany().getId() : null);

        User updated = userRepository.save(existing);
        return userMapper.toDto(updated, null);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
        return userMapper.toDto(user, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        return userRepository.findAll()
                .stream()
                .map(u -> userMapper.toDto(u, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        userRepository.deleteById(id);
    }
}
