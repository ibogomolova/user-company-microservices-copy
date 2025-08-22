package com.companymicroservice.company.service;

import com.companymicroservice.company.dto.CompanyDto;

import java.util.List;
import java.util.UUID;

public interface CompanyService {
    CompanyDto getCompanyById(UUID id);

    List<CompanyDto> getAllCompanies();

    CompanyDto createCompany(CompanyDto dto);

    CompanyDto updateCompany(UUID id, CompanyDto dto);

    void deleteCompany(UUID id);
}
