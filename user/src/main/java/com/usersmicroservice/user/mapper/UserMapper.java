package com.usersmicroservice.user.mapper;

import com.usersmicroservice.user.dto.CompanyInfoDto;
import com.usersmicroservice.user.dto.UserDto;
import com.usersmicroservice.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.UUID;

/**
 * Mapper для преобразования между сущностью {@link com.usersmicroservice.user.entity.User}
 * и DTO {@link UserDto}.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "company", source = "companyInfo")
    @Mapping(target = "id", source = "user.id")
    UserDto toDto(User user, CompanyInfoDto companyInfo);

    @Mapping(target = "companyId", ignore = true)
    User toEntity(UserDto userDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", source = "companyId")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "phone", source = "phone")
    void updateUserFromEvent(String firstName, String lastName, String phone, UUID companyId, @MappingTarget User user);
}
