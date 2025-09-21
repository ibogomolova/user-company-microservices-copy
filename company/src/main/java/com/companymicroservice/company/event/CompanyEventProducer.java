package com.companymicroservice.company.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Продюсер Kafka-сообщений для микросервиса компаний.
 * <p>
 * Отправляет события о пользователях в указанный топик Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Отправка события о пользователе в Kafka.
     *
     * @param topic название топика Kafka
     * @param event событие компании
     */
    public void sendUserEvent(String topic, CompanyEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, json);
            log.debug("Событие отправлено в Kafka: topic={}, eventType={}, userId={}", topic, event.getType(), event.getUserId());
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации Kafka-сообщения: {}", event, e);
            throw new RuntimeException("Failed to send Kafka message", e);
        }
    }
}
