package com.usersmicroservice.user.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usersmicroservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.debug("Получено сообщение из Kafka (user-events): {}", message);
        try {
            CompanyEventData eventData = objectMapper.readValue(message, CompanyEventData.class);

            switch (eventData.type) {
                case "DELETED":
                    log.info("Обработка события DELETED: удаление пользователей компании {}", eventData.companyId);
                    userService.deleteUsersByCompanyId(eventData.companyId);
                    break;
                case "CREATED":
                case "UPDATED":
                    log.info("Обработка события {}: синхронизация пользователя {} для компании {}",
                            eventData.type, eventData.userId, eventData.companyId);
                    userService.syncUserFromCompany(
                            eventData.userId,
                            eventData.firstName,
                            eventData.lastName,
                            eventData.phone,
                            eventData.companyId
                    );
                    break;
                default:
                    log.warn("Получен неизвестный тип события: {}", eventData.type);
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке Kafka-сообщения: {}", message, e);
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
        public String type;
    }
}
