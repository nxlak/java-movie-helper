package backend.academy.bot.Bot;

import backend.academy.bot.BotConfig;
import backend.academy.bot.BotState.BotState;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class Bot {

    private final TelegramBot bot;

    @Getter
    @Setter
    private Map<Long, BotState> userState = new HashMap<>();

    @Getter
    private Map<Long, Map<String, String>> userFilters = new HashMap<>();

    @Autowired
    public Bot(BotConfig botConfig) {
        this.bot = new TelegramBot(botConfig.telegramToken());
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
        // Здесь логика поиска по названию

        userState.put(chatId, BotState.IDLE);
    }

    private void handleFilterType(long chatId, String type) {
        Map<String, String> filters = userFilters.get(chatId);
        if (!"skip".equalsIgnoreCase(type)) {
            filters.put("type", type);
        }
        userState.put(chatId, BotState.WAITING_FOR_YEAR);
        sendMessage(chatId, "Введите год выпуска или skip:");
    }

    private void handleFilterYear(long chatId, String year) {
        Map<String, String> filters = userFilters.get(chatId);
        if (!"skip".equalsIgnoreCase(year)) {
            filters.put("year", year);
        }
        userState.put(chatId, BotState.WAITING_FOR_RATING);
        sendMessage(chatId, "Введите минимальный рейтинг (0-10) или skip:");
    }

    private void handleFilterRating(long chatId, String rating) {
        Map<String, String> filters = userFilters.get(chatId);
        if (!"skip".equalsIgnoreCase(rating)) {
            filters.put("rating", rating);
        }
        userState.put(chatId, BotState.WAITING_FOR_GENRE);
        sendMessage(chatId, "Введите жанр или skip:");
    }

    private void handleFilterGenre(long chatId, String genre) {
        Map<String, String> filters = userFilters.get(chatId);
        if (!"skip".equalsIgnoreCase(genre)) {
            filters.put("genre", genre);
        }

        sendMessage(chatId, "Ищем с параметрами:\n" + filters);

        userState.put(chatId, BotState.IDLE);
        userFilters.remove(chatId);
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
                userState.put(chatId, BotState.IDLE);
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
        SendMessage request = new SendMessage(chatId, text);
        SendResponse response = bot.execute(request);
        if (!response.isOk()) {
            System.out.println("Ошибка отправки сообщения в Telegram: {" + response.description() + "}");
        }
    }

}
