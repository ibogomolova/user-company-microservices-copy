package com.usersmicroservice.user.service.impl;

import com.usersmicroservice.user.client.CompanyClient;
import com.usersmicroservice.user.dto.CompanyInfoDto;
import com.usersmicroservice.user.dto.UserDto;
import com.usersmicroservice.user.entity.User;
import com.usersmicroservice.user.event.UserEvent;
import com.usersmicroservice.user.event.UserEventProducer;
import com.usersmicroservice.user.exception.UserNotFoundException;
import com.usersmicroservice.user.mapper.UserEventMapper;
import com.usersmicroservice.user.mapper.UserMapper;
import com.usersmicroservice.user.reposirory.UserRepository;
import com.usersmicroservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserEventMapper userEventMapper;
    private final UserEventProducer userEventProducer;
    private final CompanyClient companyClient;

    /**
     * Создаёт нового пользователя и отправляет событие в Kafka.
     *
     * @param userDto DTO пользователя
     * @return созданный пользователь с информацией о компании
     */
    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        log.info("Создание пользователя: {} {}", userDto.getFirstName(), userDto.getLastName());
        User user = userMapper.toEntity(userDto);

        if (userDto.getCompany() != null && userDto.getCompany().getId() != null) {
            user.setCompanyId(userDto.getCompany().getId());
        }

        User saved = userRepository.save(user);
        CompanyInfoDto company = getCompanyOrNull(saved.getCompanyId());

        sendUserEvent(saved, company, UserEvent.EventType.CREATED);

        log.info("Пользователь создан: id={}", saved.getId());
        return userMapper.toDto(saved, company);
    }

    /**
     * Обновляет пользователя и отправляет событие в Kafka.
     *
     * @param id      идентификатор пользователя
     * @param userDto новые данные
     * @return обновлённый пользователь с информацией о компании
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    @Transactional
    public UserDto update(UUID id, UserDto userDto) {
        log.info("Обновление пользователя id={}", id);
        User existing = getUserOrThrow(id);

        existing.setFirstName(userDto.getFirstName());
        existing.setLastName(userDto.getLastName());
        existing.setPhone(userDto.getPhone());
        existing.setCompanyId(userDto.getCompany() != null ? userDto.getCompany().getId() : null);

        User updated = userRepository.save(existing);
        CompanyInfoDto company = getCompanyOrNull(updated.getCompanyId());

        sendUserEvent(updated, company, UserEvent.EventType.UPDATED);

        log.info("Пользователь обновлён id={}", updated.getId());
        return userMapper.toDto(updated, company);
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
        User user = getUserOrThrow(id);
        CompanyInfoDto company = safeGetCompany(user.getCompanyId());
        return userMapper.toDto(user, company);
    }

    /**
     * Получает всех пользователей указанной компании.
     *
     * @param companyId идентификатор компании
     * @return список DTO пользователей
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
     * Получает всех пользователей с постраничной выборкой.
     * Для каждого пользователя подтягивает данные о компании.
     *
     * @param pageable параметры пагинации
     * @return страница пользователей
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> userMapper.toDto(user, safeGetCompany(user.getCompanyId())));
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
        log.warn("Удаление пользователя id={}", id);
        User user = getUserOrThrow(id);

        sendUserEvent(user, null, UserEvent.EventType.DELETED);
        userRepository.delete(user);
        log.info("Пользователь удалён id={}", id);
    }

    /**
     * Синхронизирует данные о пользователе на основе события из сервиса компаний.
     * Если пользователь найден — обновляет его данные, иначе создаёт нового.
     *
     * @param userId    ID пользователя
     * @param firstName имя
     * @param lastName  фамилия
     * @param phone     телефон
     * @param companyId ID компании
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncUserFromCompany(UUID userId, String firstName, String lastName, String phone, UUID companyId) {
        log.debug("Синхронизация пользователя из компании: userId={}, companyId={}", userId, companyId);
        userRepository.findById(userId).ifPresentOrElse(user -> {
            userMapper.updateUserFromEvent(firstName, lastName, phone, companyId, user);
            userRepository.saveAndFlush(user);
            log.info("Пользователь обновлён из события: userId={}", userId);
        }, () -> {
            User newUser = User.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .phone(phone)
                    .companyId(companyId)
                    .build();
            userRepository.saveAndFlush(newUser);
            log.info("Пользователь создан из события: {} {}", firstName, lastName);
        });
    }

    /**
     * Удаляет всех пользователей компании по её ID.
     *
     * @param companyId идентификатор компании
     */
    @Override
    public void deleteUsersByCompanyId(UUID companyId) {
        log.warn("Удаление всех пользователей компании id={}", companyId);
        userRepository.deleteByCompanyId(companyId);
        log.info("Пользователи компании {} удалены", companyId);
    }

    private User getUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
    }

    private CompanyInfoDto getCompanyOrNull(UUID companyId) {
        return companyId != null ? companyClient.getCompanyById(companyId) : null;
    }

    private CompanyInfoDto safeGetCompany(UUID companyId) {
        try {
            return getCompanyOrNull(companyId);
        } catch (Exception e) {
            log.error("Не удалось получить данные о компании id={}", companyId, e);
            return null;
        }
    }

    private void sendUserEvent(User user, CompanyInfoDto company, UserEvent.EventType type) {
        log.debug("Отправка события {} для пользователя id={}, companyId={}", type, user.getId(), user.getCompanyId());
        UserEvent event = (company != null)
                ? userEventMapper.toEvent(user, company)
                : userEventMapper.toEvent(user);
        event.setType(type);
        userEventProducer.sendCompanyEvent("company-events", event);
    }
}
