package ru.ledvanov.factory;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.ledvanov.entity.Event;
import ru.ledvanov.entity.enums.EventCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ru.ledvanov.service.enums.CallbackCommand.*;

public class KeyboardFactory {
    public static InlineKeyboardMarkup getMainMenuKeyboard(boolean isAdmin) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("🔍 Поиск по категориям", FIND_BY_CATEGORY.getCommandValue()));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("🔍 Поиск по интересам", FIND_BY_VIEWS.getCommandValue()));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createButton("🔍 Посоветуй мероприятие", FIND_SOMETHING.getCommandValue()));

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(createButton("📅 Список избранных мероприятий", FAVORITE.getCommandValue()));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        if (isAdmin) {
            List<InlineKeyboardButton> row6 = new ArrayList<>();
            row6.add(createButton("➕\uD83D\uDCC5 Добавить мероприятие", CREATE_EVENT.getCommandValue()));
            keyboard.add(row6);

            List<InlineKeyboardButton> row7 = new ArrayList<>();
            row7.add(createButton("📊👥 Аналитика", USERS_ANALYTICS.getCommandValue()));
            keyboard.add(row7);

            List<InlineKeyboardButton> row8 = new ArrayList<>();
            row8.add(createButton("\uD83D\uDCE2\uD83D\uDC65 Объявление для всех", ANNOUNCE.getCommandValue()));
            keyboard.add(row8);

            List<InlineKeyboardButton> row9 = new ArrayList<>();
            row9.add(createButton("\uD83C\uDF10 Получить мероприятия с сервиса KudaGo.com", PARSE_EVENT.getCommandValue()));
            keyboard.add(row9);
        }

        markup.setKeyboard(keyboard);

        return markup;
    }

    public static InlineKeyboardMarkup getInfoBotKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("◀️Назад", BACK_TO_MAIN.getCommandValue()));

        keyboard.add(row1);
        markup.setKeyboard(keyboard);

        return markup;
    }

    public static InlineKeyboardMarkup getCategoryKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = Arrays.stream(EventCategory.values())
                .map(category -> List.of(
                        createButton(category.getCategoryName(), "CATEGORY_" + category.name())
                ))
                .collect(Collectors.toList());

        keyboard.add(List.of(createButton("◀️Назад", BACK_TO_MAIN.getCommandValue())));
        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getEventNavigationKeyboard(Event event, EventCategory category, int currentPage, int totalPages, boolean isAdmin) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        if (currentPage > 0) {
            row1.add(createButton("⬅ Назад", "CATEGORY_" + category.name() + "_PAGE_" + (currentPage - 1)));
        }
        if (currentPage < totalPages - 1) {
            row1.add(createButton("Вперёд ➡", "CATEGORY_" + category.name() + "_PAGE_" + (currentPage + 1)));
        }

        boolean isViewed = true;
        return getInlineKeyboardMarkup(event, currentPage, keyboard, row1, isViewed, isAdmin);
    }

    public static InlineKeyboardMarkup getNotViewedEventNavigationKeyboard(Event event, int currentPage, int totalPages, boolean isAdmin) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        if (currentPage > 0) {
            row1.add(createButton("⬅ Назад", NOT_VIEWED_EVENT.getCommandValue() + (currentPage - 1)));
        }
        if (currentPage < totalPages - 1) {
            row1.add(createButton("Вперёд ➡", NOT_VIEWED_EVENT.getCommandValue() + (currentPage + 1)));
        }

        boolean isViewed = false;
        return getInlineKeyboardMarkup(event, currentPage, keyboard, row1, isViewed, isAdmin);
    }

    public static InlineKeyboardMarkup getRandomEventNavigationKeyboard(Event event, int currentPage, boolean isAdmin) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();

        boolean isViewed = false;
        return getInlineKeyboardMarkup(event, currentPage, keyboard, row1, isViewed, isAdmin);
    }

    private static InlineKeyboardMarkup getInlineKeyboardMarkup(Event event, int currentPage, List<List<InlineKeyboardButton>> keyboard, List<InlineKeyboardButton> row1, boolean isViewed, boolean isAdmin) {
        if (!row1.isEmpty()) {
            keyboard.add(row1);
        }

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("⭐ В избранное", ADD_TO_FAVORITE.getCommandValue() + event.getId()));

        if (!isViewed) {
            row2.add(createButton("ℹ Подробнее", DETAILS.getCommandValue() + event.getId() + "_PAGE_" + currentPage + "_NV"));
        } else {
            row2.add(createButton("ℹ Подробнее", DETAILS.getCommandValue() + event.getId() + "_PAGE_" + currentPage + "_CAT"));
        }

        if (isAdmin) {
            row2.add(createButton("🗑 Удалить", DELETE_EVENT.getCommandValue() + event.getId()));
        }

        keyboard.add(row2);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createButton("🏠 В главное меню", BACK_TO_MAIN.getCommandValue()));
        keyboard.add(row3);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getFavoriteEventNavigationKeyboard(Event event, int currentPage, int totalPages) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        if (currentPage > 0) {
            row.add(createButton("◀️ Назад", "FAVORITE_PAGE_" + (currentPage - 1)));
        }
        if (currentPage < totalPages - 1) {
            row.add(createButton("▶️ Вперед", "FAVORITE_PAGE_" + (currentPage + 1)));
        }
        keyboard.add(row);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("ℹ Подробнее", DETAILS.getCommandValue() + event.getId() + "_PAGE_" + currentPage + "_FAV"));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createButton("🗑 Удалить из избранного", DELETE_FAV.getCommandValue() + event.getId()));

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(createButton("🏠 В главное меню", BACK_TO_MAIN.getCommandValue()));

        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getEventDetailsNavigationKeyboard(Event event, int currentPage, String context) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1;

        if ("CAT".equals(context)) {
            row1 = new ArrayList<>();
            row1.add(createButton("◀️ Назад", "CATEGORY_" + event.getCategory().name() + "_PAGE_" + currentPage));
            keyboard.add(row1);
        } else if ("FAV".equals(context)) {
            row1 = new ArrayList<>();
            row1.add(createButton("◀️ Назад", "FAVORITE_PAGE_" + currentPage));
            keyboard.add(row1);
        }

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("🏠 В главное меню", BACK_TO_MAIN.getCommandValue()));
        keyboard.add(row2);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup goToMainKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createButton("🏠 В главное меню", BACK_TO_MAIN.getCommandValue()));

        keyboard.add(row);
        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getMainAndFindByCategoryActionKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("🔍 Поиск по категориям", FIND_BY_CATEGORY.getCommandValue()));


        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("🏠 В главное меню", BACK_TO_MAIN.getCommandValue()));

        keyboard.add(row1);
        keyboard.add(row2);
        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup reloadBotKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("\uD83D\uDD04 Взбодрить Бота", RELOAD.getCommandValue()));

        keyboard.add(row1);
        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup cancelCreateEventKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("\uD83D\uDD19 Отменить создание мероприятия", CANCEL_CREATING_EVENT.getCommandValue()));

        keyboard.add(row1);
        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getCategoryForCreateEventKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = Arrays.stream(EventCategory.values())
                .map(category -> List.of(
                        createButton(category.getCategoryName(), "CREATE_EVENT_CATEGORY_" + category.name())
                ))
                .collect(Collectors.toList());

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup confirmCreateEvent() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createButton("✅ Подтвердить", CONFIRM_CREATE_EVENT.getCommandValue()));

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("\uD83D\uDD19 Отменить", CANCEL_CREATING_EVENT.getCommandValue()));

        keyboard.add(row);
        keyboard.add(row1);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getAnalyticsKeyboardMarkup() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(createButton("🏠 В главное меню", BACK_TO_MAIN.getCommandValue())));

        return new InlineKeyboardMarkup(keyboard);
    }

    private static InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
}
