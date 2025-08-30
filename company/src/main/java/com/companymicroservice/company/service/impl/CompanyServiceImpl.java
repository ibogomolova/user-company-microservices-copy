package com.companymicroservice.company.service.impl;

import com.companymicroservice.company.dto.CompanyDto;
import com.companymicroservice.company.entity.Company;
import com.companymicroservice.company.event.CompanyEvent;
import com.companymicroservice.company.event.CompanyEventProducer;
import com.companymicroservice.company.exception.CompanyNotFoundException;
import com.companymicroservice.company.mapper.CompanyMapper;
import com.companymicroservice.company.repository.CompanyRepository;
import com.companymicroservice.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final CompanyEventProducer eventProducer;

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDto> getAllCompanies() {
        return companyRepository.findAll()
                .stream()
                .map(c -> companyMapper.toDto(c, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDto getCompanyById(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + id + " not found"));
        return companyMapper.toDto(company, null);
    }

    @Override
    public CompanyDto createCompany(CompanyDto companyDto) {
        Company saved = companyRepository.save(companyMapper.toEntity(companyDto));
        CompanyDto dto = companyMapper.toDto(saved, null);

        if (companyDto.getUsers() != null) {
            companyDto.getUsers().forEach(user -> {
                CompanyEvent event = new CompanyEvent();
                event.setUserId(user.getId());
                event.setFirstName(user.getFirstName());
                event.setLastName(user.getLastName());
                event.setPhone(user.getPhone());
                event.setCompanyId(saved.getId());
                event.setCompanyName(saved.getName());
                event.setType(CompanyEvent.EventType.CREATED);

                eventProducer.sendUserEvent("user-events", event);
            });
        }
        return dto;
    }

    @Override
    public CompanyDto updateCompany(UUID id, CompanyDto companyDto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + id + " not found"));

        company.setName(companyDto.getName());
        company.setBudget(companyDto.getBudget());

        Company updated = companyRepository.save(company);

        if (companyDto.getUsers() != null) {
            companyDto.getUsers().forEach(user -> {
                CompanyEvent event = new CompanyEvent();
                event.setUserId(user.getId());
                event.setFirstName(user.getFirstName());
                event.setLastName(user.getLastName());
                event.setPhone(user.getPhone());
                event.setCompanyId(updated.getId());
                event.setCompanyName(updated.getName());
                event.setType(CompanyEvent.EventType.UPDATED);

                eventProducer.sendUserEvent("user-events", event);
            });
        }
        return companyMapper.toDto(updated, null);
    }

    @Override
    public void deleteCompany(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + id + " not found"));

        if (company.getUserIds() != null) {
            company.getUserIds().forEach(userId -> {
                CompanyEvent event = new CompanyEvent();
                event.setUserId(userId);
                event.setType(CompanyEvent.EventType.DELETED);
                eventProducer.sendUserEvent("user-events", event);
            });
        }
        companyRepository.deleteById(id);
    }
}
