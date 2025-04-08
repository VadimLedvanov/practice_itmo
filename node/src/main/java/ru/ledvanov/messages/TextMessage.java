package ru.ledvanov.messages;

public class TextMessage {
    public static final String START_MESSAGE = "Привет! \uD83D\uDC4B Я помогу тебе выбрать интересное мероприятие. Используй кнопки ниже, чтобы найти что-то по душе!";
    public static final String INFO_BOT_MESSAGE =
            "Привет! Я бот, который поможет тебе найти что-то интересное! \uD83C\uDFAD\uD83C\uDFA4\uD83C\uDFAC\n" +
            "Я работаю 24/7, не устаю и никогда не опаздываю! \uD83D\uDE0E\n" +
            "\n" +
            "\uD83D\uDE80 Что я умею:\n" +
            "\n" +
            "\t1. Искать мероприятия по категориям\n" +
            "\n" +
            "\t2. Советовать что-то классное\n" +
            "\n" +
            "\t3. Сохранять твои любимые события\n" +
            "\n" +
            "Если вдруг что-то не так — это, конечно, не моя вина... но можно написать разработчику! \uD83D\uDE05" +
            "\n" +
            "Связь со мной: @ledvanov";
    public static final String CATEGORY_MESSAGE = "\uD83C\uDFAD Что тебя интересует? Выбери категорию, и я покажу тебе подходящие мероприятия!";
    public static final String EVENT_MESSAGE =
            "🎭 <b>Мероприятие #%d</b>\n\n" +
                    "📌  <code>Название: %s\n" +
                    "📍 Локация: %s\n" +
                    "🗓 Дата: %s\n" +
                    "🕰 Время: %s\n" +
                    "📂 Категория: %s</code>";

    public static final String DETAILS_EVENT_MESSAGE =
            "🎭 <b>Мероприятие:</b>\n\n" +
                    "📌 <code>Название: %s</code>\n" +
                    "📍 <code>Локация: %s</code>\n" +
                    "🗓 <code>Дата: %s</code>\n" +
                    "🗓 <code>Время: %s</code>\n" +
                    "📂 <code>Категория: %s</code>\n\n" +
                    "📖 <code>Описание: %s</code>";

    public static final String UNKNOWN_MESSAGE =
            "Список доступных команд:\n" +
            "/start - Главное меню";
}
