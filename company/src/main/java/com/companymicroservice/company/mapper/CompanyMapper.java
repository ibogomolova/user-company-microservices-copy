package com.companymicroservice.company.mapper;

import com.companymicroservice.company.dto.CompanyDto;
import com.companymicroservice.company.dto.UserInfoDto;
import com.companymicroservice.company.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    @Mapping(target = "users", source = "userInfoList")
    CompanyDto toDto(Company company, List<UserInfoDto> userInfoList);

    Company toEntity(CompanyDto dto);
}
