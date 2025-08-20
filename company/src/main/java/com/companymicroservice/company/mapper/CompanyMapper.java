package com.companymicroservice.company.mapper;

import com.companymicroservice.company.dto.CompanyDto;
import com.companymicroservice.company.entity.Company;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    CompanyDto toDto(Company company);
    Company toEntity(CompanyDto companyDto);
}
