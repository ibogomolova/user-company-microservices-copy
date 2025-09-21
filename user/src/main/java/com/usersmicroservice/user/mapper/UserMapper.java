package com.usersmicroservice.user.mapper;

import com.usersmicroservice.user.dto.CompanyInfoDto;
import com.usersmicroservice.user.dto.UserDto;
import com.usersmicroservice.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper для преобразования между сущностью {@link com.usersmicroservice.user.entity.User}
 * и DTO {@link UserDto}.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Преобразует сущность User в DTO.
     *
     * @param user сущность пользователя
     * @param companyInfo информация о компании
     * @return DTO пользователя
     */
    @Mapping(target = "company", source = "companyInfo")
    @Mapping(target = "id", source = "user.id")
    UserDto toDto(User user, CompanyInfoDto companyInfo);

    /**
     * Преобразует DTO пользователя в сущность.
     *
     * @param userDto DTO пользователя
     * @return сущность пользователя
     */
    @Mapping(target = "companyId", ignore = true)
    User toEntity(UserDto userDto);
}
