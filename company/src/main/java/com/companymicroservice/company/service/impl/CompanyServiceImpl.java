package com.companymicroservice.company.service.impl;

import com.companymicroservice.company.dto.CompanyDto;
import com.companymicroservice.company.entity.Company;
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
        return companyMapper.toDto(saved, null);
    }

    @Override
    public CompanyDto updateCompany(UUID id, CompanyDto companyDto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company with id " + id + " not found"));

        company.setName(companyDto.getName());
        company.setBudget(companyDto.getBudget());

        Company updated = companyRepository.save(company);
        return companyMapper.toDto(updated, null);
    }

    @Override
    public void deleteCompany(UUID id) {
        if (!companyRepository.existsById(id)) {
            throw new CompanyNotFoundException("Company with id " + id + " not found");
        }
        companyRepository.deleteById(id);
    }
}
