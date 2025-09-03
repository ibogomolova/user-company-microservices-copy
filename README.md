# User-company-microservices

Кратко: Два Spring Boot микросервиса (user-service и company-service), которые хранят данные о пользователях и компаниях, взаимодействуют через REST (FeignClient) и управляются через API Gateway. Данные хранятся в PostgreSQL, события синхронизируются через Kafka. Всё разворачивается с помощью Docker Compose.

---

## Содержание README
1. Описание
2. Фичи
3. Архитектура и стек
4. Быстрый старт (локально)
5. Переменные окружения / конфигурация
6. Docker / docker-compose
7. База данных и миграции
8. Тестирование
9. Эндпоинты
10. Рекомендации по безопасности
11. Развитие проекта / contribution
12. Troubleshooting (частые проблемы)
13. Контакты / лицензия

---

## 1. Описание
Проект состоит из двух микросервисов:
user-service — хранение и управление пользователями.
company-service — хранение и управление компаниями.

Сервисы взаимодействуют через REST (FeignClient). API Gateway предоставляет единую точку входа для клиентов.
При возврате данных:
пользователи содержат объект компании, а не companyId;
компании содержат список пользователей, а не userIds.

## 2. Фичи
- CRUD API для пользователей и компаний
- REST API с интеграцией через FeignClient
- Kafka — для событий между сервисами
- API Gateway (Spring Cloud Gateway)
- PostgreSQL для хранения данных
- Liquibase для миграций
- MapStruct для маппинга DTO ↔️ Entity
- Docker Compose для запуска всей системы

## 3. Архитектура и стек
- Java 21+
- Spring Boot (Web, Data JPA, Cloud OpenFeign, Cloud Gateway)
- PostgreSQL
- Liquibase (миграции)
- Kafka (event-driven взаимодействие)
- Lombok
- MapStruct
- Docker + Docker Compose


### Структура репозитория

user-company-microservices
├─ company/       # company-service 
├─ gateway/       # gateway-service
├─ user/          # user-service 
├─ docker-compose.yml
└─ README.md

## 4. Быстрый старт (локально)
### Требования
- Java 17+
- Maven 3.8+
- PostgreSQL
- Docker, docker-compose

### Сборка и запуск
1. Клонируй репозиторий и перейди в папку проекта:
bash
git clone <https://github.com/ibogomolova/user-company-microservices.git>
cd user-company-microservices

Настрой переменные окружения (см. раздел 5). 
Альтернативно отредактируй src/main/resources/application.properties (не храни секреты в репозитории!).

Запусти PostgreSQL локально и создай БД (или используй docker-compose, пример ниже).

Собери все три сервиса:

./mvnw clean package -DskipTests

## 5. Переменные окружения / конфигурация
Пример application.properties (замени значения на реальные через env vars):

### company-service (application.properties):
spring.application.name=company
server.port=8082

# Postgres
spring.datasource.url=jdbc:postgresql://localhost:5432/${COMPANY_M_DB}
spring.datasource.username=${COMPANY_M_ADMIN}
spring.datasource.password=${COMPANY_M_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.liquibase.default-schema=public

# Liquibase
spring.liquibase.change-log=classpath:db/changelog-master.yaml

# JPA & Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Kafka
spring.kafka.bootstrap-servers=${SPRING_KAFKA_BOOTSTRAP-SERVERS:kafka:9092}
spring.kafka.consumer.group-id=company-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer

# User client
user.service.url=http://user-service:8081

### user-service (application.properties):

Совет: храните пароли и секреты в переменных окружения или secret manager.

## 6. Docker / docker-compose

В проекте есть docker-compose.yml, который поднимает:
postgres_user (для user-service)
postgres_company (для company-service)
zookeeper + kafka
user-service
company-service
gateway-service

Запуск:
docker compose up --build

## 7. База данных и миграции
Проект содержит Liquibase миграции в src/main/resources/db/. При запуске приложение ожидает, что схема совпадает с миграциями (ddl-auto=validate).

## 8. Тестирование
Запуск тестов:
./mvnw test

Простой endpoint для проверки SMTP уже есть:
GET /test-email — отправляет тестовое письмо на адрес, указанный в коде (в TestMailController). Отредактируй адрес или используй контроллер как шаблон для тестирования.

Отладка почты: включи spring.mail.properties.mail.debug=true в properties, чтобы видеть SMTP-диалог в логах.

## 9. Основные эндпоинты (быстрый список)
(Автоматически извлечены из контроллеров — проверить в коде для деталей запросов/тел)

user-service

POST /users — создать пользователя

GET /users/{id} — получить пользователя

PUT /users/{id} — обновить пользователя

DELETE /users/{id} — удалить пользователя

GET /users — список всех пользователей

GET /users/by-company/{companyId} — пользователи конкретной компании

company-service

POST /companies — создать компанию

GET /companies/{id} — получить компанию

PUT /companies/{id} — обновить компанию

DELETE /companies/{id} — удалить компанию

GET /companies — список всех компаний

(Подробные схемы DTO находятся в src/main/java/.../dto.)

## 10. Рекомендации по безопасности

Добавить Spring Security + JWT для авторизации.

Хранить секреты (пароли, ключи) только в env vars / secret manager.

DTO использовать вместо Entity для сериализации.

## 11. Contribution / Development

Форкни репозиторий

Создай ветку для своей фичи

Покрой изменения тестами

Сделай Pull Request

Открывай Pull Request с описанием изменений и шагами для тестирования.

## 12. Troubleshooting (частые проблемы)

500 при GET /users → добавить @JsonIgnoreProperties(ignoreUnknown = true) в CompanyInfoDto.

FeignClient не находит сервис → проверь company.service.url и сетевые алиасы в Docker Compose.

Проблемы с Kafka → убедись, что kafka сервис поднялся и доступен на kafka:9092.

Миграции падают → проверь ddl-auto=validate и совпадение схемы БД с changelog Liquibase.

# Полезные команды

 Сборка jar
./mvnw clean package -DskipTests

 Запуск тестов
./mvnw test

## 13. Контакты / лицензия(позже)
