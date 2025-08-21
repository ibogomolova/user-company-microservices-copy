package com.usersmicroservice.user.mapper;

import com.usersmicroservice.user.dto.CompanyInfoDto;
import com.usersmicroservice.user.dto.UserDto;
import com.usersmicroservice.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "company", source = "companyInfo")
    @Mapping(target = "id", source = "user.id")
    UserDto toDto(User user, CompanyInfoDto companyInfo);

    User toEntity(UserDto userDto);
}
