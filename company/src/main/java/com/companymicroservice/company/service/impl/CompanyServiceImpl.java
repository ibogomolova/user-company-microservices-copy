package com.companymicroservice.company.service.impl;

import com.companymicroservice.company.client.UserClient;
import com.companymicroservice.company.dto.CompanyDto;
import com.companymicroservice.company.dto.UserInfoDto;
import com.companymicroservice.company.entity.Company;
import com.companymicroservice.company.event.CompanyEvent;
import com.companymicroservice.company.event.CompanyEventProducer;
import com.companymicroservice.company.exception.CompanyNotFoundException;
import com.companymicroservice.company.mapper.CompanyEventMapper;
import com.companymicroservice.company.mapper.CompanyMapper;
import com.companymicroservice.company.repository.CompanyRepository;
import com.companymicroservice.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Реализация сервиса компаний.
 * <p>
 * Содержит бизнес-логику для работы с компаниями:
 * - CRUD-операции;
 * - синхронизацию пользователей через Kafka;
 * - управление связями пользователей и компаний.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final CompanyEventMapper companyEventMapper;
    private final CompanyEventProducer eventProducer;
    private final UserClient userClient;

    /**
     * Получает список всех компаний вместе с пользователями.
     *
     * @return список DTO компаний
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CompanyDto> getAllCompanies(Pageable pageable) {
        log.debug("Запрос списка компаний, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return companyRepository.findAll(pageable)
                .map(company -> companyMapper.toDto(company, getUsersForCompany(company.getId())));
    }

    /**
     * Получает компанию по её ID.
     *
     * @param id идентификатор компании
     * @return DTO компании
     * @throws CompanyNotFoundException если компания не найдена
     */
    @Override
    @Transactional(readOnly = true)
    public CompanyDto getCompanyById(UUID id) {
        log.debug("Запрос компании по id={}", id);
        Company company = getCompanyOrThrow(id);
        return companyMapper.toDto(company, getUsersForCompany(company.getId()));
    }

    /**
     * Создаёт новую компанию и отправляет события
     * о добавленных пользователях в Kafka.
     *
     * @param companyDto DTO компании
     * @return созданная компания
     */
    @Override
    public CompanyDto createCompany(CompanyDto companyDto) {
        log.info("Создание компании: {}", companyDto.getName());
        Company company = companyMapper.toEntity(companyDto);
        Company saved = companyRepository.save(company);

        List<UserInfoDto> users = processUsers(companyDto.getUsers(), saved, CompanyEvent.EventType.CREATED);
        log.info("Компания создана: id={}, name={}", saved.getId(), saved.getName());
        return companyMapper.toDto(saved, users);
    }

    /**
     * Обновляет компанию и синхронизирует пользователей через Kafka.
     *
     * @param id         идентификатор обновляемой компании
     * @param companyDto DTO с новыми данными
     * @return обновлённая компания
     * @throws CompanyNotFoundException если компания не найдена
     */
    @Override
    public CompanyDto updateCompany(UUID id, CompanyDto companyDto) {
        log.info("Обновление компании: id={}", id);
        Company company = getCompanyOrThrow(id);
        companyMapper.updateEntityFromDto(companyDto, company);

        Company updated = companyRepository.save(company);

        List<UserInfoDto> users = processUsers(companyDto.getUsers(), updated, CompanyEvent.EventType.UPDATED);
        log.info("Компания обновлена: id={}", updated.getId());
        return companyMapper.toDto(updated, users);
    }

    /**
     * Удаляет компанию и отправляет события о том,
     * что пользователи больше не принадлежат ей.
     *
     * @param id идентификатор компании
     * @throws CompanyNotFoundException если компания не найдена
     */
    @Override
    public void deleteCompany(UUID id) {
        log.warn("Удаление компании id={}", id);
        Company company = getCompanyOrThrow(id);

        if (company.getUserIds() != null) {
            company.getUserIds().forEach(userId -> sendUserDeletionEvent(userId, company));
        }
        companyRepository.deleteById(id);
        log.info("Компания удалена id={}", id);
    }

    /**
     * Добавляет пользователя в список сотрудников компании.
     *
     * @param userId    идентификатор пользователя
     * @param companyId идентификатор компании
     * @throws CompanyNotFoundException если компания не найдена
     */
    @Override
    public void addUserToCompany(UUID userId, UUID companyId) {
        log.debug("Добавление пользователя {} в компанию {}", userId, companyId);
        Company company = getCompanyOrThrow(companyId);

        if (company.getUserIds() == null) {
            company.setUserIds(new ArrayList<>());
        }
        if (!company.getUserIds().contains(userId)) {
            company.getUserIds().add(userId);
            companyRepository.saveAndFlush(company);
            log.info("Пользователь {} добавлен в компанию {}", userId, companyId);
        } else {
            log.debug("Пользователь {} уже есть в компании {}", userId, companyId);
        }
    }

    /**
     * Удаляет пользователя из всех компаний, где он числится.
     *
     * @param userId идентификатор пользователя
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeUserFromCompany(UUID userId) {
        log.warn("Удаление пользователя {} из всех компаний", userId);
        companyRepository.findAll().stream()
                .filter(company -> company.getUserIds() != null && company.getUserIds().contains(userId))
                .forEach(company -> {
                    company.getUserIds().remove(userId);
                    companyRepository.saveAndFlush(company);
                    log.info("Пользователь {} удалён из компании {}", userId, company.getId());
                });
    }

    private Company getCompanyOrThrow(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + id + " not found"));
    }

    private List<UserInfoDto> getUsersForCompany(UUID companyId) {
        try {
            return userClient.getUsersByCompany(companyId);
        } catch (Exception e) {
            log.error("Не удалось получить пользователей для компании {}", companyId, e);
            return new ArrayList<>();
        }
    }

    private List<UserInfoDto> processUsers(List<UserInfoDto> users,
                                           Company company,
                                           CompanyEvent.EventType eventType) {
        List<UserInfoDto> result = new ArrayList<>();
        if (users == null) return result;

        for (UserInfoDto user : users) {
            UserInfoDto fullUser = fetchOrCreateUser(user);
            sendUserEvent(fullUser, company, eventType);
            result.add(fullUser);
        }
        return result;
    }

    private UserInfoDto fetchOrCreateUser(UserInfoDto user) {
        if (user.getId() != null) {
            try {
                return userClient.getUserById(user.getId());
            } catch (Exception e) {
                return user;
            }
        } else {
            user.setId(UUID.randomUUID());
            return user;
        }
    }

    private void sendUserEvent(UserInfoDto user, Company company, CompanyEvent.EventType type) {
        log.debug("Отправка события {} для пользователя {} в компанию {}", type, user.getId(), company.getId());
        CompanyEvent event = companyEventMapper.toEvent(user, company);
        event.setType(type);
        eventProducer.sendUserEvent("user-events", event);
    }

    private void sendUserDeletionEvent(UUID userId, Company company) {
        log.debug("Отправка события удаления пользователя {} из компании {}", userId, company.getId());
        CompanyEvent event = companyEventMapper.toDeleteEvent(userId, company);
        event.setType(CompanyEvent.EventType.DELETED);
        eventProducer.sendUserEvent("user-events", event);
    }
}
