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
    public static InlineKeyboardMarkup getMainMenuKeyboard() {
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

        List<InlineKeyboardButton> row5 = new ArrayList<>();
        row5.add(createButton("⚙ Обо мне", INFO.getCommandValue()));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        keyboard.add(row5);
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

    public static InlineKeyboardMarkup getEventNavigationKeyboard(Event event, EventCategory category, int currentPage, int totalPages) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        if (currentPage > 0) {
            row1.add(createButton("⬅ Назад", "CATEGORY_" + category.name() + "_PAGE_" + (currentPage - 1)));
        }
        if (currentPage < totalPages - 1) {
            row1.add(createButton("Вперёд ➡", "CATEGORY_" + category.name() + "_PAGE_" + (currentPage + 1)));
        }
        if (!row1.isEmpty()) {
            keyboard.add(row1);
        }

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("⭐ В избранное", ADD_TO_FAVORITE.getCommandValue() + event.getId()));
        row2.add(createButton("ℹ Подробнее", DETAILS.getCommandValue() + event.getId() + "_PAGE_" + currentPage + "_CAT"));

        keyboard.add(row2);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createButton("🏠 В главное меню", BACK_TO_MAIN.getCommandValue()));
        keyboard.add(row3);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getFavoriteEventNavigationKeyboard(Event event, int currentPage, int totalPages) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
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
        row3.add(createButton("🏠 В главное меню", BACK_TO_MAIN.getCommandValue()));

        keyboard.add(row2);
        keyboard.add(row3);

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getEventDetailsNavigationKeyboard(Event event, int currentPage, String context) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1;
        if ("CAT".equals(context)) {
            row1 = new ArrayList<>();
            row1.add(createButton("◀️ Назад", "CATEGORY_" + event.getCategory().name() + "_PAGE_" + currentPage));
            keyboard.add(row1);
        }

        else if ("FAV".equals(context)) {
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

    private static InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
}
