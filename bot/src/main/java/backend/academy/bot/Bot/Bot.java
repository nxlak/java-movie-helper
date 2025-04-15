package backend.academy.bot.Bot;

import backend.academy.bot.BotConfig;
import backend.academy.bot.BotState.BotState;
import backend.academy.scrapper.DTO.MovieInfo;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Bot {

    private final TelegramBot bot;

    private final WebClient webClient;

    @Getter
    @Setter
    private Map<Long, BotState> userState = new HashMap<>();

    @Getter
    private Map<Long, Map<String, String>> userFilters = new HashMap<>();

    @Autowired
    public Bot(BotConfig botConfig, WebClient webClient) {
        this.bot = new TelegramBot(botConfig.telegramToken());
        this.webClient = webClient;
    }

    @PostConstruct
    public void init() {
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                handleUpdate(update);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void handleFilmName(long chatId, String filmName) {
        webClient.get()
            .uri("/films/{name}", filmName)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<MovieInfo>>() {})
            .subscribe(movies -> {
                if (movies.isEmpty()) {
                    sendMessage(chatId, "Фильмы не найдены.");
                } else {
                    movies.forEach(movie -> {
                        String message = String.format(
                            "🎬 *%s* (%d)\n\n" +
                                "⭐ Рейтинг: %.1f\n" +
                                "🎭 Жанры: %s\n" +
                                "🌍 Страны: %s\n" +
                                "📖 Описание: %s\n\n" +
                                "🖼 [Постер](%s)",
                            movie.getName(),
                            movie.getYear(),
                            movie.getRatingKp() != null ? movie.getRatingKp() : 0.0,
                            String.join(", ", movie.getGenres()),
                            String.join(", ", movie.getCountries()),
                            movie.getDescription() != null ?
                                movie.getDescription().substring(0, Math.min(200, movie.getDescription().length())) + "..." :
                                "Нет описания",
                            movie.getPosterUrl() != null ? movie.getPosterUrl() : ""
                        );
                        sendMessage(chatId, message);
                    });
                }
                userState.put(chatId, BotState.IDLE);
            }, error -> {
                sendMessage(chatId, "❌ Ошибка поиска");
                userState.put(chatId, BotState.IDLE);
            });
    }

    private void handleFilterType(long chatId, String type) {
        Map<String, String> filters = userFilters.get(chatId);
        if (!"skip".equalsIgnoreCase(type)) {
            filters.put("type", type);
        } else {
            filters.remove("type");
        }
        userState.put(chatId, BotState.WAITING_FOR_YEAR);
        sendMessage(chatId, "Введите диапазон года выпуска (пример: 2014-2024) или skip:");
    }

    private void handleFilterYear(long chatId, String year) {
        Map<String, String> filters = userFilters.get(chatId);
        if (!"skip".equalsIgnoreCase(year)) {
            filters.put("year", year);
        } else {
            filters.remove("year");
        }
        userState.put(chatId, BotState.WAITING_FOR_RATING);
        sendMessage(chatId, "Введите диапазон рейтинг (пример: 6-9) или skip:");
    }

    private void handleFilterRating(long chatId, String rating) {
        Map<String, String> filters = userFilters.get(chatId);
        if (!"skip".equalsIgnoreCase(rating)) {
            filters.put("rating", rating);
        } else {
            filters.remove("rating");
        }
        userState.put(chatId, BotState.WAITING_FOR_GENRE);
        sendMessage(chatId, "Введите жанр или skip:");
    }

    private void handleFilterGenre(long chatId, String genre) {
        Map<String, String> filters = userFilters.get(chatId);

        if ("skip".equalsIgnoreCase(genre)) {
            genre = null;
        }

        String finalGenre = genre;
        webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/filter")
                .queryParamIfPresent("type", Optional.ofNullable(filters.get("type")))
                .queryParamIfPresent("year", Optional.ofNullable(filters.get("year")))
                .queryParamIfPresent("rating", Optional.ofNullable(filters.get("rating")))
                .queryParamIfPresent("genre", Optional.ofNullable(finalGenre))
                .build())
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<MovieInfo>>() {})
            .subscribe(movies -> {
                if (movies.isEmpty()) {
                    sendMessage(chatId, "По вашему запросу ничего не найдено 😔");
                } else {
                    movies.stream()
                        .limit(5)
                        .forEach(movie -> {
                            String message = String.format(
                                "🍿 *%s* (%d)\n\n" +
                                    "⭐ Рейтинг: %.1f\n" +
                                    "🎭 Жанры: %s\n" +
                                    "🌍 Страны: %s\n" +
                                    "📖 Описание: %s\n\n" +
                                    "🖼 [Постер](%s)",
                                movie.getName(),
                                movie.getYear(),
                                movie.getRatingKp() != null ? movie.getRatingKp() : 0.0,
                                String.join(", ", movie.getGenres()),
                                String.join(", ", movie.getCountries()),
                                movie.getDescription() != null ?
                                    movie.getDescription().substring(0, Math.min(200, movie.getDescription().length())) + "..." :
                                    "Нет описания",
                                movie.getPosterUrl() != null ? movie.getPosterUrl() : ""
                            );
                            sendMessage(chatId, message);
                        });
                }
                userState.put(chatId, BotState.IDLE);
                userFilters.remove(chatId);
            }, error -> {
                sendMessage(chatId, "❌ Ошибка фильтрации");
                userState.put(chatId, BotState.IDLE);
                userFilters.remove(chatId);
            });
    }

    private void handleUpdate(Update update) {
        if (update.message() == null || update.message().text() == null) return;

        String messageText = update.message().text();
        long chatId = update.message().chat().id();

        switch (messageText) {
            case "/help" -> {
                String response = "📜 Список команд:\n\n" +
                    "1. /start - Регистрация и краткое руководство\n" +
                    "2. /help - Это сообщение\n" +
                    "3. /title - Поиск по имени\n" +
                    "4. /filters - Поиск с параметрами\n" +
                    "5. /random - Случайный фильм/сериал\n\n" +
                    "🎯 Просто введи команду и следуй подсказкам!";
                sendMessage(chatId, response);
                return;
            }
            case "/start" -> {
                String response = "🎬 Добро пожаловать в КиноПоиск-бота!\n\n" +
                    "Здесь ты можешь:\n" +
                    "🔍 Искать фильмы и сериалы по названию\n" +
                    "🎲 Получать случайные рекомендации\n" +
                    "⚙️ Настраивать поиск по жанрам, году, рейтингу\n\n" +
                    "Используй /help для списка команд!";
                sendMessage(chatId, response);
                return;
            }
            case "/title" -> {
                userState.put(chatId, BotState.WAITING_FOR_FILM_NAME);
                sendMessage(chatId, "Введите название фильма/сериала:");
                return;
            }
            case "/filters" -> {
                userState.put(chatId, BotState.WAITING_FOR_TYPE);
                userFilters.put(chatId, new HashMap<>());
                sendMessage(chatId, "Введите тип (фильм/сериал) или skip:");
                return;
            }
            case "/random" -> {
                webClient.get()
                    .uri("/random")
                    .retrieve()
                    .bodyToMono(MovieInfo.class)
                    .subscribe(movie -> {
                        String message = String.format(
                            "🎲 *Случайный фильм*\n\n" +
                                "🎬 *%s* (%d)\n\n" +
                                "⭐ Рейтинг: %.1f\n" +
                                "🎭 Жанры: %s\n" +
                                "🌍 Страны: %s\n" +
                                "📖 Описание: %s\n\n" +
                                "🖼 [Постер](%s)",
                            movie.getName(),
                            movie.getYear(),
                            movie.getRatingKp() != null ? movie.getRatingKp() : 0.0,
                            String.join(", ", movie.getGenres()),
                            String.join(", ", movie.getCountries()),
                            movie.getDescription() != null ?
                                movie.getDescription().substring(0, Math.min(300, movie.getDescription().length())) + "..." :
                                "Нет описания",
                            movie.getPosterUrl() != null ? movie.getPosterUrl() : ""
                        );
                        sendMessage(chatId, message);
                    }, error -> {
                        sendMessage(chatId, "❌ Ошибка при получении случайного фильма");
                    });
                return;
            }
            default -> {
                BotState currentState = userState.getOrDefault(chatId, BotState.IDLE);
                switch (currentState) {
                    case WAITING_FOR_FILM_NAME -> handleFilmName(chatId, messageText);
                    case WAITING_FOR_TYPE -> handleFilterType(chatId, messageText);
                    case WAITING_FOR_YEAR -> handleFilterYear(chatId, messageText);
                    case WAITING_FOR_RATING -> handleFilterRating(chatId, messageText);
                    case WAITING_FOR_GENRE -> handleFilterGenre(chatId, messageText);
                    default -> sendMessage(chatId, "Неизвестная команда! Используйте /help");
                }
            }
        }

    }

    public void sendMessage(long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text)
            .parseMode(ParseMode.Markdown)
            .disableWebPagePreview(false);
        SendResponse response = bot.execute(request);
        if (!response.isOk()) {
            System.out.println("Ошибка отправки сообщения: {" + response.description() + "}");
        }
    }

}
