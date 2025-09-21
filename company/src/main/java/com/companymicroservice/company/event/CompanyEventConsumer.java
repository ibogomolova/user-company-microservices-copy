package com.companymicroservice.company.event;

import com.companymicroservice.company.service.CompanyService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.debug("Получено сообщение из Kafka: {}", message);
        try {
            UserEventData eventData = objectMapper.readValue(message, UserEventData.class);

            switch (eventData.type) {
                case "CREATED":
                case "UPDATED":
                    log.info("Обработка события {}: userId={}, companyId={}", eventData.type, eventData.userId, eventData.companyId);
                    companyService.addUserToCompany(eventData.userId, eventData.companyId);
                    break;
                case "DELETED":
                    log.info("Обработка события DELETED: userId={}", eventData.userId);
                    companyService.removeUserFromCompany(eventData.userId);
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
    private static class UserEventData {
        public UUID userId;
        public UUID companyId;
        public String type;
    }
}
