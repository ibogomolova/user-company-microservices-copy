package com.usersmicroservice.user.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Продюсер Kafka-сообщений для микросервиса пользователей.
 * <p>
 * Отправляет события о пользователях и их компаниях в указанный топик Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Отправка события о компании в Kafka.
     *
     * @param topic название топика Kafka
     * @param event событие пользователя
     */
    public void sendCompanyEvent(String topic, UserEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, json);
            log.debug("Событие отправлено в Kafka: topic={}, eventType={}, userId={}, companyId={}",
                    topic, event.getType(), event.getUserId(), event.getCompanyId());
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации Kafka-сообщения: {}", event, e);
            throw new RuntimeException("Failed to send Kafka message", e);
        }
    }
}

