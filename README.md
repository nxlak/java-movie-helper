# Java Movie Helper

Проект **Java Movie Helper** представляет собой систему для поиска и рекомендаций фильмов и сериалов в Telegram. Состоит из двух модулей:

* **scrapper** – REST API сервис на Spring Boot, взаимодействующий с API Kinopoisk и обрабатывающий данные о фильмах
* **bot** – Telegram-бот на Spring Boot и библиотеке `com.pengrad.telegrambot`, предоставляющий удобный чат-интерфейс для пользователей

## Начало работы

### 1. Клонировать репозиторий

```bash
git clone https://github.com/nxlak/java-movie-helper
cd java-movie-helper
````

### 2. Настроить переменные окружения

* `KINOPOISK_TOKEN` – ваш API-ключ Kinopoisk
* `TELEGRAM_TOKEN`  – токен вашего Telegram-бота

### 3. Сборка и запуск

Собрать оба модуля и прогнать тесты:

```bash
./mvnw clean verify
```

Запустить модули по отдельности:

```bash
# Запуск scrapper (порт 8081)
cd scrapper
../mvnw spring-boot:run

# В другом терминале: запуск bot (порт 8080)
cd ../bot
../mvnw spring-boot:run
```

## Использование Telegram-бота

В боте доступны команды:

- `/start`   – регистрация и приветствие
- `/help`    – вывести список команд
- `/title`   – поиск по названию фильма/сериала
- `/filters` – поиск с параметрами (тип, год, рейтинг, жанр, страна)
- `/random`  – получить случайную рекомендацию
