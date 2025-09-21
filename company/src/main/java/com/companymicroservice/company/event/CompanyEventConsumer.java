package com.companymicroservice.company.event;

import com.companymicroservice.company.service.CompanyService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Консьюмер Kafka-сообщений для микросервиса компаний.
 * <p>
 * Слушает топик "company-events" и выполняет действия с пользователями в компании:
 * - добавление;
 * - обновление;
 * - удаление.
 */
@Component
@RequiredArgsConstructor
public class CompanyEventConsumer {

    private final ObjectMapper objectMapper;
    private final CompanyService companyService;

    /**
     * Обработка входящих сообщений из Kafka.
     *
     * @param message JSON-сообщение с данными о пользователе и компании
     */
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

    /**
     * Внутренний класс для десериализации JSON-сообщений.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class UserEventData {
        public UUID userId;
        public UUID companyId;
        public String type;
    }
}
