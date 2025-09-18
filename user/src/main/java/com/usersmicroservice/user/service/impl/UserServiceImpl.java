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

/**
 * Реализация сервиса пользователей.
 * <p>
 * Содержит бизнес-логику для работы с пользователями:
 * - CRUD-операции;
 * - синхронизацию с сервисом компаний через Kafka;
 * - получение связанных данных о компании через Feign-клиент.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserEventProducer userEventProducer;
    private final CompanyClient companyClient;

    /**
     * Создаёт нового пользователя и отправляет событие в Kafka.
     *
     * @param userDto DTO пользователя
     * @return созданный пользователь
     */
    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        User user = userMapper.toEntity(userDto);

        if (userDto.getCompany() != null && userDto.getCompany().getId() != null) {
            user.setCompanyId(userDto.getCompany().getId());
        }

        User saved = userRepository.save(user);

        UserEvent event = new UserEvent();
        event.setUserId(saved.getId());
        event.setFirstName(saved.getFirstName());
        event.setLastName(saved.getLastName());
        event.setPhone(saved.getPhone());

        if (user.getCompanyId() != null) {
            CompanyInfoDto companyInfo = companyClient.getCompanyById(user.getCompanyId());
            event.setCompanyId(companyInfo.getId());
            event.setCompanyName(companyInfo.getName());
        }
        event.setType(UserEvent.EventType.CREATED);

        userEventProducer.sendCompanyEvent("company-events", event);

        return userMapper.toDto(
                saved,
                event.getCompanyId() != null ? companyClient.getCompanyById(user.getCompanyId()) : null);
    }

    /**
     * Обновляет пользователя и отправляет событие в Kafka.
     *
     * @param id      идентификатор пользователя
     * @param userDto новые данные
     * @return обновлённый пользователь
     * @throws UserNotFoundException если пользователь не найден
     */
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

        if (userDto.getCompany() != null && userDto.getCompany().getId() != null) {
            event.setCompanyId(userDto.getCompany().getId());
            event.setCompanyName(userDto.getCompany().getName());
        }

        event.setType(UserEvent.EventType.UPDATED);

        userEventProducer.sendCompanyEvent("company-events", event);

        return userMapper.toDto(
                updated,
                event.getCompanyId() != null ? companyClient.getCompanyById(existing.getCompanyId()) : null
        );
    }

    /**
     * Получает пользователя по его ID.
     *
     * @param id идентификатор пользователя
     * @return DTO пользователя с информацией о компании
     * @throws UserNotFoundException если пользователь не найден
     */
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

    /**
     * Получает всех пользователей компании.
     *
     * @param companyId идентификатор компании
     * @return список пользователей
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByCompanyId(UUID companyId) {
        return userRepository.findByCompanyId(companyId)
                .stream()
                .map(user -> userMapper.toDto(user, null))
                .collect(Collectors.toList());
    }

    /**
     * Получает список всех пользователей вместе с компаниями.
     *
     * @return список пользователей
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        return userRepository.findAll()
                .stream()
                .map(user -> {
                    CompanyInfoDto company;
                    try {
                        company = user.getCompanyId() != null ? companyClient.getCompanyById(user.getCompanyId()) : null;
                    } catch (Exception e) {
                        company = null;
                    }
                    return userMapper.toDto(user, company);
                })
                .collect(Collectors.toList());
    }

    /**
     * Удаляет пользователя и отправляет событие об удалении в Kafka.
     *
     * @param id идентификатор пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
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

    /**
     * Синхронизирует данные о пользователе на основе события из сервиса компаний.
     * Если пользователь найден — обновляет, иначе создаёт нового.
     *
     * @param userId    ID пользователя
     * @param firstName имя
     * @param lastName  фамилия
     * @param phone     телефон
     * @param companyId ID компании
     */
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

    /**
     * Удаляет всех пользователей компании по её ID.
     *
     * @param companyId идентификатор компании
     */
    @Override
    public void deleteUsersByCompanyId(UUID companyId) {
        userRepository.deleteByCompanyId(companyId);
    }
}
