package com.usersmicroservice.user.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usersmicroservice.user.dto.CompanyInfoDto;
import com.usersmicroservice.user.dto.UserDto;
import com.usersmicroservice.user.event.UserEvent;
import com.usersmicroservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final ObjectMapper objectMapper;
    private final UserService userService;

    @KafkaListener(topics = "user-events", groupId = "user-group")
    public void handleCompanyUserEvents(String message) throws JsonProcessingException {
        UserEvent event = objectMapper.readValue(message, UserEvent.class);

        UserDto dto = new UserDto();
        dto.setId(event.getUserId());
        dto.setFirstName(event.getFirstName());
        dto.setLastName(event.getLastName());
        dto.setPhone(event.getPhone());

        CompanyInfoDto company = new CompanyInfoDto();
        company.setId(event.getCompanyId());
        company.setName(event.getCompanyName());
        dto.setCompany(company);

        switch (event.getType()) {
            case CREATED -> userService.create(dto);
            case UPDATED -> userService.update(event.getUserId(), dto);
            case DELETED -> userService.delete(event.getUserId());
        }
    }
}


