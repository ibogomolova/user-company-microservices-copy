package com.companymicroservice.company.service;

import com.companymicroservice.company.dto.CompanyDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CompanyService {
    CompanyDto getCompanyById(UUID id);

    Page<CompanyDto> getAllCompanies(Pageable pageable);

    CompanyDto createCompany(CompanyDto dto);

    CompanyDto updateCompany(UUID id, CompanyDto dto);

    void deleteCompany(UUID id);

    void addUserToCompany(UUID userId, UUID companyId);

    void removeUserFromCompany(UUID userId);
}
