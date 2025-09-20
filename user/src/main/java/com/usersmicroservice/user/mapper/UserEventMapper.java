package com.usersmicroservice.user.mapper;

import com.usersmicroservice.user.dto.CompanyInfoDto;
import com.usersmicroservice.user.entity.User;
import com.usersmicroservice.user.event.UserEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserEventMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "phone", source = "user.phone")
    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyName", source = "company.name")
    @Mapping(target = "type", ignore = true)
    UserEvent toEvent(User user, CompanyInfoDto company);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "phone", source = "user.phone")
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "companyName", ignore = true)
    @Mapping(target = "type", ignore = true)
    UserEvent toEvent(User user);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "companyName", ignore = true)
    @Mapping(target = "type", ignore = true)
    UserEvent toDeleteEvent(User user);
}
