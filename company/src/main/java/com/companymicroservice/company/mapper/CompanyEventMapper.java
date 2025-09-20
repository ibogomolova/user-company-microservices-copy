package com.companymicroservice.company.mapper;

import com.companymicroservice.company.dto.UserInfoDto;
import com.companymicroservice.company.entity.Company;
import com.companymicroservice.company.event.CompanyEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CompanyEventMapper {

    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyName", source = "company.name")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "phone", source = "user.phone")
    @Mapping(target = "type", ignore = true)
    CompanyEvent toEvent(UserInfoDto user, Company company);

    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "companyName", source = "company.name")
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "type", ignore = true)
    CompanyEvent toDeleteEvent(UUID userId, Company company);
}
