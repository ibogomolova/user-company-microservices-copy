package com.companymicroservice.company.mapper;

import com.companymicroservice.company.dto.CompanyDto;
import com.companymicroservice.company.dto.UserInfoDto;
import com.companymicroservice.company.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper для преобразования между сущностью {@link com.companymicroservice.company.entity.Company}
 * и DTO {@link CompanyDto}.
 */
@Mapper(componentModel = "spring")
public interface CompanyMapper {

    /**
     * Преобразует сущность компании в DTO.
     *
     * @param company      сущность компании
     * @param userInfoList список пользователей компании
     * @return DTO компании
     */
    @Mapping(target = "users", source = "userInfoList")
    CompanyDto toDto(Company company, List<UserInfoDto> userInfoList);

    /**
     * Преобразует DTO компании в сущность.
     *
     * @param dto DTO компании
     * @return сущность компании
     */
    Company toEntity(CompanyDto dto);
}
