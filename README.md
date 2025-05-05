# Java Movie Helper

The **Java Movie Helper** project is a system for searching and recommending movies and TV shows via Telegram. It consists of two modules:

* **scrapper** – A Spring Boot REST API service that interacts with the Kinopoisk API and processes movie data
* **bot** – A Telegram bot built with Spring Boot and the `com.pengrad.telegrambot` library, providing a user-friendly chat interface

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/nxlak/java-movie-helper
cd java-movie-helper
```

### 2. Set Environment Variables

* `KINOPOISK_TOKEN` – your Kinopoisk API key
* `TELEGRAM_TOKEN`  – your Telegram bot token

### 3. Build and Run

To build both modules and run tests:

```bash
./mvnw clean verify
```

To run the modules separately:

```bash
# Start scrapper (port 8081)
cd scrapper
../mvnw spring-boot:run

# In another terminal: start bot (port 8080)
cd ../bot
../mvnw spring-boot:run
```

## Using the Telegram Bot

The bot supports the following commands:

* `/start`   – register and get a welcome message
* `/help`    – display the list of available commands
* `/title`   – search by movie/TV show title
* `/filters` – search with filters (type, year, rating, genre, country)
* `/random`  – get a random recommendation
