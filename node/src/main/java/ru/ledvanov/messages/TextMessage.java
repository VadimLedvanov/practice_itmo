package ru.ledvanov.messages;

public class TextMessage {
    public static final String START_MESSAGE = "🏠 <b>Главное меню</b>\n\n" +
            "✅ Вам доступны <b>три</b> вида поиска:\n\n1.\tПо выбранной категории\n2.\tПо вашим интересам\n3.\tСлучайный выбор\n\n" +
            "📣 Используй кнопки <b>ниже</b>, чтобы найти интересное мероприятие!";
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
    public static final String CATEGORY_MESSAGE = "\uD83C\uDFAD Что вас интересует? Выберите категорию, и я подберу подходящие мероприятия!";
    public static final String EVENT_MESSAGE =
            "🎭 <b>Мероприятие #%d:</b>\n\n" +
                    "📌 <code>Название: %s</code>\n\n" +
                    "📍 <code>Адрес: %s</code>\n\n" +
                    "🗓 <code>Дата: %s</code>\n\n" +
                    "🗓 <code>Время: %s</code>\n\n" +
                    "📂 <code>Категория: %s</code>\n\n" +
                    "\uD83D\uDD17 <code>Ссылка на источник: </code>%s\n\n" +
                    "\uD83D\uDCCD <code>Геопозиция: </code>%s\n\n";

    public static final String DETAILS_EVENT_MESSAGE =
            "🎭 <b>Мероприятие:</b>\n\n" +
                    "📌 <code>Название: %s</code>\n\n" +
                    "📍 <code>Адрес: %s</code>\n\n" +
                    "🗓 <code>Дата: %s</code>\n\n" +
                    "🗓 <code>Время: %s</code>\n\n" +
                    "📂 <code>Категория: %s</code>\n\n" +
                    "\uD83D\uDD17 <code>Ссылка на источник: </code>%s\n\n" +
                    "\uD83D\uDCCD <code>Геопозиция: </code>%s\n\n" +
                    "📖 <code>Описание: %s</code>";

    public static final String RANDOM_EVENT_MESSAGE =
            "🎭 <b>Мероприятие:</b>\n\n" +
                    "📌 <code>Название: %s</code>\n\n" +
                    "📍 <code>Адрес: %s</code>\n\n" +
                    "🗓 <code>Дата: %s</code>\n\n" +
                    "🗓 <code>Время: %s</code>\n\n" +
                    "📂 <code>Категория: %s</code>\n\n" +
                    "\uD83D\uDD17 <code>Ссылка на источник: </code>%s\n\n" +
                    "\uD83D\uDCCD <code>Геопозиция: </code>%s\n\n";

    public static final String UNKNOWN_MESSAGE =
            "Список доступных команд:\n" +
            "/start - Главное меню";

    public static final String FAVORITE_EVENT_IS_EXIST = "🚨 Данное мероприятие уже находится в списке избранных";
    public static final String SUCCESS_ADDING_TO_FAVORITE = "✅ Мероприятие успешно добавлено в избранное";
    public static final String SUCCESS_DELETE_FROM_FAVORITE_MESSAGE = "✅ Мероприятие успешно удалено из избранного";
    public static final String SUCCESS_DELETE_EVENT_MESSAGE = "✅ Мероприятие успешно удалено";
    public static final String CANNOT_FIND_EVENT_BY_VIEWS = "⚠️ Не удалось найти ни одного мероприятия.\n" +
            "\nВам необходимо просмотреть мероприятия через кнопку \"Подробнее\", которую можно увидеть у мероприятий, найденных по категориям c помощью кнопки ниже.\n" +
            "\nВ противном случае я не пойму, какие у Вас интересы";

    public static final String SOMETHING_WRONG_MESSAGE = "😅 Ой...  \n" +
            "Кажется, Я немного устал!\n" +
            "\nВзбодри меня и попробуй заново";

    public static final String ENTER_EVENT_NAME_MESSAGE = "Введите название мероприятия:";
    public static final String ENTER_EVENT_LOCATION_MESSAGE = "Введите адрес мероприятия (например, метро Нарвская, ул. Нарвский проспект 13Б:";
    public static final String ENTER_EVENT_DESCRIPTION_MESSAGE = "Введите описание мероприятия:";
    public static final String ENTER_EVENT_DATE_MESSAGE = "Введите дату и время мероприятия (например, 17.04.2025 12:00):";
    public static final String ENTER_EVENT_CATEGORY_MESSAGE = "Выберите категорию мероприятия:";
    public static final String WRONG_EVENT_DATE_FORMAT = "Введенная дата не соответствует формату дд.мм.гг чч.мм\n" +
            "Введите еще раз дату и время мероприятия (например, 17.04.2025 12:00):";
    public static final String CONFIRM_CREATE_EVENT_MESSAGE = "Ваше мероприятие создано:\n" +
            "📌 <code>Название: %s</code>\n" +
            "📍 <code>Локация: %s</code>\n" +
            "🗓 <code>Дата: %s</code>\n" +
            "🗓 <code>Время: %s</code>\n" +
            "📂 <code>Категория: %s</code>\n\n" +
            "📖 <code>Описание: %s</code>";
    public static final String SUCCESS_CREATING_EVENT_MESSAGE = "✅ Мероприятие успешно создано";
    public static final String ANALYTICS_MESSAGE =
            "📊 <b>Пользователи</b>\n" +
            "\n" +
            "👥 <code>Всего пользователей: %d</code>\n" +
            "🆕 <code>Новых за сегодня: %d</code>\n" +
            "📅 <code>За неделю: %d</code>\n" +
            "🗓 <code>За месяц: %d</code>\n" +
            "\n" +
            "🎭 <b>Мероприятия</b>:\n" +
            "\n" +
            "📌 <code>Всего мероприятий: %s</code>\n" +
            "🎵 <code>Концерты: %s</code>\n" +
            "🎭 <code>Театр: %s</code>\n" +
            "🎬 <code>Кино: %s</code>\n" +
            "🖼 <code>Выставки: %s</code>\n" +
            "💼 <code>Бизнес-встречи: %s</code>\n" +
            "🏛️ <code>Экскурсии: %s</code>\n";

    public static final String NOTIFICATION_MESSAGE = "✅ Уведомление было успешно отправлено %d пользователям: %s";
    public static final String ENTER_ANNOUNCE_TEXT = "Введите текст уведомления:";
    public static final String START_PROCESS = "✅ Запустился процесс по получению мероприятий с сервиса KudaGo.com\n\nПожалуйста, подождите несколько секунд.";
}
