package com.companymicroservice.company.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Продюсер Kafka-сообщений для микросервиса компаний.
 * <p>
 * Отправляет события о пользователях в указанный топик Kafka.
 */
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
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to send Kafka message", e);
        }
    }
}
