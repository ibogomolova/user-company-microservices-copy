package com.usersmicroservice.user.service.impl;

import com.usersmicroservice.user.client.CompanyClient;
import com.usersmicroservice.user.dto.CompanyInfoDto;
import com.usersmicroservice.user.dto.UserDto;
import com.usersmicroservice.user.entity.User;
import com.usersmicroservice.user.event.UserEvent;
import com.usersmicroservice.user.event.UserEventProducer;
import com.usersmicroservice.user.exception.UserNotFoundException;
import com.usersmicroservice.user.mapper.UserMapper;
import com.usersmicroservice.user.reposirory.UserRepository;
import com.usersmicroservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    private final UserEventProducer userEventProducer;
    private final CompanyClient companyClient;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        User saved = userRepository.save(user);

        UserEvent event = new UserEvent();
        event.setUserId(saved.getId());
        event.setFirstName(saved.getFirstName());
        event.setLastName(saved.getLastName());
        event.setPhone(saved.getPhone());

        if (userDto.getCompany() != null) {
            event.setCompanyId(userDto.getCompany().getId());
            event.setCompanyName(userDto.getCompany().getName());
        }
        event.setType(UserEvent.EventType.CREATED);

        userEventProducer.sendCompanyEvent("company-events", event);

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

        UserEvent event = new UserEvent();
        event.setUserId(updated.getId());
        event.setFirstName(updated.getFirstName());
        event.setLastName(updated.getLastName());
        event.setPhone(updated.getPhone());

        if (userDto.getCompany() != null) {
            event.setCompanyId(userDto.getCompany().getId());
            event.setCompanyName(userDto.getCompany().getName());
        }

        event.setType(UserEvent.EventType.UPDATED);

        userEventProducer.sendCompanyEvent("company-events", event);

        return userMapper.toDto(updated, null);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
        CompanyInfoDto company = user.getCompanyId() != null
                ? companyClient.getCompanyById(user.getCompanyId())
                : null;
        return userMapper.toDto(user, company);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByCompanyId(UUID companyId) {
        return userRepository.findByCompanyId(companyId)
                .stream()
                .map(user -> userMapper.toDto(user, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        return userRepository.findAll()
                .stream()
                .map(user -> {
                    CompanyInfoDto company = user.getCompanyId() != null
                            ? companyClient.getCompanyById(user.getCompanyId())
                            : null;
                    return userMapper.toDto(user, company);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));

        UserEvent event = new UserEvent();
        event.setUserId(user.getId());
        event.setType(UserEvent.EventType.DELETED);

        userEventProducer.sendCompanyEvent("company-events", event);

        userRepository.deleteById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncUserFromCompany(UUID userId, String firstName, String lastName, String phone, UUID companyId) {
        userRepository.findById(userId).ifPresentOrElse(user -> {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhone(phone);
            user.setCompanyId(companyId);
            userRepository.saveAndFlush(user);
        }, () -> {
            User newUser = new User();
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setPhone(phone);
            newUser.setCompanyId(companyId);
            userRepository.saveAndFlush(newUser);
        });
    }

    @Override
    public void deleteUsersByCompanyId(UUID companyId) {
        userRepository.deleteByCompanyId(companyId);
    }
}
