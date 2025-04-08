package ru.ledvanov.service.impl;

import lombok.extern.log4j.Log4j;
import org.glassfish.grizzly.utils.Pair;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.ledvanov.dao.AppUserDAO;
import ru.ledvanov.dao.EventDAO;
import ru.ledvanov.dao.FavoriteDAO;
import ru.ledvanov.entity.AppUser;
import ru.ledvanov.entity.Event;
import ru.ledvanov.entity.Favorite;
import ru.ledvanov.entity.enums.EventCategory;
import ru.ledvanov.service.MainService;
import ru.ledvanov.service.ProducerService;
import ru.ledvanov.service.enums.TextCommand;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static ru.ledvanov.factory.KeyboardFactory.*;
import static ru.ledvanov.messages.TextMessage.*;
import static ru.ledvanov.service.enums.CallbackCommand.*;
import static ru.ledvanov.service.enums.TextCommand.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final FavoriteDAO favoriteDAO;
    private final EventDAO eventDAO;
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

    public MainServiceImpl(ProducerService producerService, AppUserDAO appUserDAO, FavoriteDAO favoriteDAO, EventDAO eventDAO) {
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.favoriteDAO = favoriteDAO;
        this.eventDAO = eventDAO;
    }

    @Override
    public void processTextMessage(Update update) {
        findOrSaveAppUser(update);

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        String output;

        TextCommand textCommand = TextCommand.fromValue(text);
        if (START.equals(textCommand)) {
            output = START_MESSAGE;
            sendCallbackAnswer(output, chatId, getMainMenuKeyboard());
        }  else {
            output = UNKNOWN_MESSAGE;
            sendTextAnswer(output, chatId);
        }
    }

    @Override
    public void processCallbackMessage(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String callbackData = callbackQuery.getData();

        String output;
        InlineKeyboardMarkup markup;
        Long chatId = callbackQuery.getMessage().getChatId();

        if (FAVORITE.getCommandValue().equals(callbackData)) {
            Pair<String, InlineKeyboardMarkup> pair = getFavoriteEvent(update, 0);
            output = pair.getFirst();
            markup = pair.getSecond();

            sendEditMessageCallbackAnswer(chatId, callbackQuery.getMessage().getMessageId(), output, markup);
        } else if (callbackData.startsWith(FAVORITE_PAGE.getCommandValue())) {
            String[] parts = callbackData.split("_PAGE_");
            int page = Integer.parseInt(parts[1]);

            Pair<String, InlineKeyboardMarkup> pair = getFavoriteEvent(update, page);
            output = pair.getFirst();
            markup = pair.getSecond();

            sendEditMessageCallbackAnswer(chatId, callbackQuery.getMessage().getMessageId(), output, markup);
        } else if (callbackData.startsWith(DETAILS.getCommandValue())) {
            String[] parts = callbackData.split("_PAGE_");
            UUID eventId = UUID.fromString(parts[0].replace("DETAILS_", ""));
            int currentPage = Integer.parseInt(parts[1].split("_")[0]);
            String context = parts[1].split("_")[1];

            Pair<String, InlineKeyboardMarkup> pair = getDetails(eventId, currentPage, context);
            output = pair.getFirst();
            markup = pair.getSecond();

            sendEditMessageCallbackAnswer(chatId, callbackQuery.getMessage().getMessageId(), output, markup);
        } else if (FIND_BY_CATEGORY.getCommandValue().equals(callbackData)) {
            Pair<String, InlineKeyboardMarkup> pair = getCategories();
            output = pair.getFirst();
            markup = pair.getSecond();

            sendEditMessageCallbackAnswer(chatId, callbackQuery.getMessage().getMessageId(), output, markup);
        } else if (callbackData.startsWith(CHOOSE_CATEGORY.getCommandValue()) && !callbackData.contains("_PAGE_")) {
            EventCategory selectedCategory = EventCategory.valueOf(callbackData.replace(CHOOSE_CATEGORY.getCommandValue(), ""));

            Pair<String, InlineKeyboardMarkup> eventCard = getEventCard(selectedCategory, 0);
            output = eventCard.getFirst();
            markup = eventCard.getSecond();

            sendEditMessageCallbackAnswer(chatId, callbackQuery.getMessage().getMessageId(), output, markup);
        } else if (callbackData.startsWith(CHOOSE_CATEGORY.getCommandValue()) && callbackData.contains("_PAGE_")) {
            String[] parts = callbackData.split("_PAGE_");
            EventCategory selectedCategory = EventCategory.valueOf(parts[0].replace(CHOOSE_CATEGORY.getCommandValue(), ""));
            int page = Integer.parseInt(parts[1]);

            Pair<String, InlineKeyboardMarkup> event = getEventCard(selectedCategory, page);
            output = event.getFirst();
            markup = event.getSecond();

            sendEditMessageCallbackAnswer(chatId, callbackQuery.getMessage().getMessageId(), output, markup);
        } else if (callbackData.startsWith(ADD_TO_FAVORITE.getCommandValue())) {
            UUID eventId = UUID.fromString(callbackData.replace(ADD_TO_FAVORITE.getCommandValue(), ""));
            addEventToFavoriteList(update, eventId);
        } else if (FIND_BY_VIEWS.getCommandValue().equals(callbackData)) {
            getEventByViews();
        } else if (FIND_SOMETHING.getCommandValue().equals(callbackData)) {
            getSomethingEvents();
        } else if (INFO.getCommandValue().startsWith(callbackData)) {
            Pair<String, InlineKeyboardMarkup> pair = getInfo();
            output = pair.getFirst();
            markup = pair.getSecond();
            sendEditMessageCallbackAnswer(chatId, callbackQuery.getMessage().getMessageId(), output, markup);
        } else if (BACK_TO_MAIN.getCommandValue().equals(callbackData)) {
            output = START_MESSAGE;
            markup = getMainMenuKeyboard();
            sendEditMessageCallbackAnswer(chatId, callbackQuery.getMessage().getMessageId(), output, markup);
        } else {
            log.warn("Unknown callback data: " + callbackData);
        }
    }

    private Pair<String, InlineKeyboardMarkup> getDetails(UUID eventId, int currentPage, String context) {
        Event event = eventDAO.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Мероприятие не найдено"));

        String output = String.format(DETAILS_EVENT_MESSAGE,
                event.getName(),
                event.getLocation(),
                event.getDate().format(dateFormat),
                event.getDate().format(timeFormat),
                event.getCategory().getCategoryName(),
                event.getDescription());

        InlineKeyboardMarkup markup = getEventDetailsNavigationKeyboard(event, currentPage, context);
        return new Pair<>(output, markup);
    }

    public void addEventToFavoriteList(Update update, UUID eventId) {
        Event event = eventDAO.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Мероприятие не найдено"));

        User telegramUser = update.getCallbackQuery().getFrom();
        AppUser appUser = appUserDAO.findByTelegramUserId(telegramUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        Favorite favorite = Favorite.builder()
                .appUser(appUser)
                .event(event)
                .createdAt(LocalDateTime.now())
                .build();

        appUser.getFavorites().add(favorite);

        appUserDAO.save(appUser);
    }

    public Pair<String, InlineKeyboardMarkup> getEventCard(EventCategory category, int page) {
        Page<Event> eventPage = searchEventsByCategory(category, page, 1);

        if (eventPage.isEmpty()) {
            return new Pair<>("🚫 В этой категории пока нет мероприятий.", goToMainKeyboard());
        }

        Event event = eventPage.getContent().get(0);
        String output = String.format(EVENT_MESSAGE, page + 1,
                event.getName(),
                event.getLocation(),
                event.getDate().format(dateFormat),
                event.getDate().format(timeFormat),
                event.getCategory().getCategoryName());

        InlineKeyboardMarkup markup = getEventNavigationKeyboard(event, category, page, eventPage.getTotalPages());

        return new Pair<>(output, markup);
    }

    private Page<Event> searchEventsByCategory(EventCategory category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return eventDAO.findEventsByCategory(category, pageable);
    }

    private void getSomethingEvents() {
        log.debug("Поиск мероприятий временно недоступен");
    }

    private void getEventByViews() {
        log.debug("Поиск мероприятий по интересам временно недоступен");
    }

    private Pair<String, InlineKeyboardMarkup> getFavoriteEvent(Update update, int page) {
        User telegramUser = update.getCallbackQuery().getFrom();
        AppUser appUser = appUserDAO.findByTelegramUserId(telegramUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        Page<Favorite> favoritePage = searchFavoriteEvents(appUser, page, 1);
        if (favoritePage.isEmpty()) {
            return new Pair<>("🚫 У вас нет избранных мероприятий.", goToMainKeyboard());
        }

        Event event = favoritePage.getContent().get(0).getEvent();
        String output = String.format("\t<b>Избранные мероприятия</b>\n\n" + EVENT_MESSAGE, page + 1,
                event.getName(),
                event.getLocation(),
                event.getDate().format(dateFormat),
                event.getDate().format(timeFormat),
                event.getCategory().getCategoryName());

        InlineKeyboardMarkup markup = getFavoriteEventNavigationKeyboard(event ,page, favoritePage.getTotalPages());
        return new Pair<>(output, markup);
    }

    private Page<Favorite> searchFavoriteEvents(AppUser appUser, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return favoriteDAO.findByAppUser(appUser, pageable);
    }

    private Pair<String, InlineKeyboardMarkup> getCategories() {
        return new Pair<>(CATEGORY_MESSAGE, getCategoryKeyboard());
    }

    private Pair<String, InlineKeyboardMarkup> getInfo() {
        return new Pair<>(INFO_BOT_MESSAGE, getInfoBotKeyboard());
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        Optional<AppUser> optional = appUserDAO.findByTelegramUserId(telegramUser.getId());
        if (optional.isEmpty()) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .favorites(new ArrayList<>())
                    .build();

            return appUserDAO.save(transientAppUser);
        }
        return optional.get();
    }

    private void sendTextAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }

    private void sendCallbackAnswer(String output, Long chatId, InlineKeyboardMarkup markup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        sendMessage.setReplyMarkup(markup);
        producerService.producerAnswer(sendMessage);
    }

    private void sendEditMessageCallbackAnswer(Long chatId, Integer messageId, String output, InlineKeyboardMarkup markup) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(output);
        editMessage.setReplyMarkup(markup);
        editMessage.setParseMode("HTML");

        producerService.produceEditedAnswer(editMessage);
    }
}
