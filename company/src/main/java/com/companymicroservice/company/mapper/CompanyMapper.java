package com.companymicroservice.company.mapper;

import com.companymicroservice.company.dto.CompanyDto;
import com.companymicroservice.company.dto.UserInfoDto;
import com.companymicroservice.company.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Mapper для преобразования между сущностью {@link com.companymicroservice.company.entity.Company}
 * и DTO {@link CompanyDto}.
 */
@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(target = "users", source = "userInfoList")
    CompanyDto toDto(Company company, List<UserInfoDto> userInfoList);

    @Mapping(target = "userIds", ignore = true)
    Company toEntity(CompanyDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userIds", ignore = true)
    void updateEntityFromDto(CompanyDto dto, @MappingTarget Company entity);
}
