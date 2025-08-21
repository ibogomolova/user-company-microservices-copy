package com.companymicroservice.company.service.impl;

import com.companymicroservice.company.client.UserClient;
import com.companymicroservice.company.dto.CompanyDto;
import com.companymicroservice.company.dto.UserInfoDto;
import com.companymicroservice.company.entity.Company;
import com.companymicroservice.company.exception.CompanyNotFoundException;
import com.companymicroservice.company.mapper.CompanyMapper;
import com.companymicroservice.company.repository.CompanyRepository;
import com.companymicroservice.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final UserClient userClient;

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDto> getAllCompanies() {
        return companyRepository.findAll()
                .stream()
                .map(c -> companyMapper.toDto(c, fetchUsersForCompany(c.getUserIds())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDto getCompanyById(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + id + " not found"));
        return companyMapper.toDto(company, fetchUsersForCompany(company.getUserIds()));
    }

    @Override
    public CompanyDto createCompany(CompanyDto companyDto) {
        Company saved = companyRepository.save(companyMapper.toEntity(companyDto));
        return companyMapper.toDto(saved, fetchUsersForCompany(saved.getUserIds()));
    }

    @Override
    public CompanyDto updateCompany(UUID id, CompanyDto companyDto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + id + " not found"));

        company.setName(companyDto.getName());
        company.setBudget(companyDto.getBudget());

        Company updated = companyRepository.save(company);
        return companyMapper.toDto(updated, fetchUsersForCompany(updated.getUserIds()));
    }

    @Override
    public void deleteCompany(UUID id) {
        if (!companyRepository.existsById(id)) {
            throw new CompanyNotFoundException("Company with id " + id + " not found");
        }
        companyRepository.deleteById(id);
    }

    private List<UserInfoDto> fetchUsersForCompany(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyList();

        List<UserInfoDto> allUsers = userClient.getAllUsers()
                .stream()
                .filter(u -> userIds.contains(u.getId()))
                .map(u -> new UserInfoDto(u.getId(), u.getFirstName(), u.getLastName(), u.getPhone()))
                .collect(Collectors.toList());

        return allUsers;
    }
}
