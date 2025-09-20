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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Реализация сервиса компаний.
 * <p>
 * Содержит бизнес-логику для работы с компаниями:
 * - CRUD-операции;
 * - синхронизацию пользователей через Kafka;
 * - управление связями пользователей и компаний.
 */
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
        return companyRepository.findAll(pageable)
                .map(company -> {
                    List<UserInfoDto> users = userClient.getUsersByCompany(company.getId());
                    return companyMapper.toDto(company, users);
                });
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
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + id + " not found"));
        List<UserInfoDto> users = userClient.getUsersByCompany(company.getId());
        return companyMapper.toDto(company, users);
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
        Company company = companyMapper.toEntity(companyDto);
        Company saved = companyRepository.save(company);

        List<UserInfoDto> users = new ArrayList<>();
        if (companyDto.getUsers() != null) {
            users = companyDto.getUsers().stream().map(user -> {
                UserInfoDto fullUser;
                if (user.getId() != null) {
                    try {
                        fullUser = userClient.getUserById(user.getId());
                    } catch (Exception e) {
                        fullUser = user;
                    }
                } else {
                    user.setId(UUID.randomUUID());
                    fullUser = user;
                }
                CompanyEvent event = companyEventMapper.toEvent(fullUser, saved);
                event.setType(CompanyEvent.EventType.CREATED);

                eventProducer.sendUserEvent("user-events", event);
                return fullUser;
            }).collect(Collectors.toList());
        }
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
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + id + " not found"));

        companyMapper.updateEntityFromDto(companyDto, company);

        Company updated = companyRepository.save(company);

        List<UserInfoDto> users = new ArrayList<>();
        if (companyDto.getUsers() != null) {
            users = companyDto.getUsers().stream().map(user -> {
                UserInfoDto fullUser;
                if (user.getId() != null) {
                    try {
                        fullUser = userClient.getUserById(user.getId());
                    } catch (Exception e) {
                        fullUser = user;
                    }
                } else {
                    user.setId(UUID.randomUUID());
                    fullUser = user;
                }
                CompanyEvent event = companyEventMapper.toEvent(fullUser, updated);
                event.setType(CompanyEvent.EventType.UPDATED);

                eventProducer.sendUserEvent("user-events", event);

                return fullUser;
            }).collect(Collectors.toList());
        }
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
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + id + " not found"));

        if (company.getUserIds() != null) {
            company.getUserIds().forEach(userId -> {
                CompanyEvent event = companyEventMapper.toDeleteEvent(userId, company);
                event.setType(CompanyEvent.EventType.DELETED);
                eventProducer.sendUserEvent("user-events", event);
            });
        }
        companyRepository.deleteById(id);
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
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + companyId + " not found"));

        if (company.getUserIds() == null) {
            company.setUserIds(new ArrayList<>());
        }
        if (!company.getUserIds().contains(userId)) {
            company.getUserIds().add(userId);
            companyRepository.saveAndFlush(company);
        }
    }

    /**
     * Удаляет пользователя из всех компаний, где он числится.
     *
     * @param userId идентификатор пользователя
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeUserFromCompany(UUID userId) {
        List<Company> companiesWithUser = companyRepository.findAll().stream()
                .filter(company -> company.getUserIds() != null && company.getUserIds().contains(userId))
                .toList();

        for (Company company : companiesWithUser) {
            company.getUserIds().remove(userId);
            companyRepository.saveAndFlush(company);
        }
    }
}
