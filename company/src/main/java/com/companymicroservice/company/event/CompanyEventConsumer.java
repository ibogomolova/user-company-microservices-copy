package com.companymicroservice.company.event;

import com.companymicroservice.company.service.CompanyService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CompanyEventConsumer {

    private final ObjectMapper objectMapper;
    private final CompanyService companyService;

    @KafkaListener(topics = "company-events", groupId = "company-group")
    public void consumeUserEvent(String message) {
        try {
            UserEventData eventData = objectMapper.readValue(message, UserEventData.class);

            switch (eventData.type) {
                case "CREATED":
                case "UPDATED":
                    companyService.addUserToCompany(eventData.userId, eventData.companyId);
                    break;
                case "DELETED":
                    companyService.removeUserFromCompany(eventData.userId);
                    break;
                default:
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class UserEventData {
        public UUID userId;
        public UUID companyId;
        public String companyName;
        public String type;
    }
}
