package com.connecting_microservices.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Конфигурация WebClient для взаимодействия микросервисов.
 * <p>
 * Определяет бин {@link WebClient.Builder}, который используется
 * для выполнения асинхронных HTTP-запросов между сервисами.
 */
@Configuration
public class WebClientConfig {

    /**
     * Создает и регистрирует бин {@link WebClient.Builder}.
     *
     * @return билдер для настройки WebClient
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
