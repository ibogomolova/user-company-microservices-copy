package com.usersmicroservice.user.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usersmicroservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Консьюмер Kafka-сообщений для микросервиса пользователей.
 * <p>
 * Слушает топик "user-events" и выполняет действия:
 * - синхронизация пользователя при создании или обновлении;
 * - удаление пользователей при удалении компании.
 */
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final ObjectMapper objectMapper;
    private final UserService userService;

    /**
     * Обработка входящих сообщений из Kafka.
     *
     * @param message JSON-сообщение с данными о компании и пользователях
     */
    @KafkaListener(topics = "user-events", groupId = "user-group")
    public void consumeCompanyEvent(String message) {
        try {
            CompanyEventData eventData = objectMapper.readValue(message, CompanyEventData.class);

            switch (eventData.type) {
                case "DELETED":
                    userService.deleteUsersByCompanyId(eventData.companyId);
                    break;
                case "CREATED":
                case "UPDATED":
                    userService.syncUserFromCompany(
                            eventData.userId,
                            eventData.firstName,
                            eventData.lastName,
                            eventData.phone,
                            eventData.companyId
                    );
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
    private static class CompanyEventData {
        public UUID userId;
        public String firstName;
        public String lastName;
        public String phone;
        public UUID companyId;
        public String companyName;
        public String type;
    }
}
