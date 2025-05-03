package ru.ledvanov.service.impl;

import lombok.extern.log4j.Log4j;
import org.glassfish.grizzly.utils.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.ledvanov.dao.AppUserDAO;
import ru.ledvanov.dao.EventDAO;
import ru.ledvanov.dao.FavoriteDAO;
import ru.ledvanov.dao.ViewDAO;
import ru.ledvanov.dto.AnnounceDto;
import ru.ledvanov.dto.EventDto;
import ru.ledvanov.entity.AppUser;
import ru.ledvanov.entity.Event;
import ru.ledvanov.entity.Favorite;
import ru.ledvanov.entity.View;
import ru.ledvanov.entity.enums.EventCategory;
import ru.ledvanov.schedule.EventScheduler;
import ru.ledvanov.service.MainService;
import ru.ledvanov.service.ProducerService;
import ru.ledvanov.service.enums.TextCommand;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static ru.ledvanov.dto.AnnounceDto.AnnounceStep.*;
import static ru.ledvanov.dto.EventDto.StepState.*;
import static ru.ledvanov.dto.EventDto.StepState.CONFIRM;
import static ru.ledvanov.factory.KeyboardFactory.*;
import static ru.ledvanov.messages.TextMessage.*;
import static ru.ledvanov.service.enums.CallbackCommand.*;
import static ru.ledvanov.service.enums.TextCommand.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {
    @Value("${bot.admins}")
    private String admins;
    private List<Long> adminList;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final FavoriteDAO favoriteDAO;
    private final EventDAO eventDAO;
    private final ViewDAO viewDAO;
    private final EventScheduler eventScheduler;
    private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    private final Map<Long, EventDto> createEventState = new HashMap<>();
    private final Map<Long, AnnounceDto> createAnnounceState = new HashMap<>();

    @PostConstruct
    public void initAdminList() {
        adminList = Arrays.stream(admins.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    public MainServiceImpl(ProducerService producerService, AppUserDAO appUserDAO, FavoriteDAO favoriteDAO, EventDAO eventDAO, ViewDAO viewDAO, EventScheduler eventScheduler) {
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
        this.favoriteDAO = favoriteDAO;
        this.eventDAO = eventDAO;
        this.viewDAO = viewDAO;
        this.eventScheduler = eventScheduler;
    }

    @Override
    public void processTextMessage(Update update) {
        findOrSaveAppUser(update);

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Long telegramUserId = update.getMessage().getFrom().getId();
        String output;

        AnnounceDto announceDto;
        if (createAnnounceState.containsKey(telegramUserId)) {
            announceDto = createAnnounceState.get(telegramUserId);

            if (ASK_ANNOUNCEMENT_TEXT == announceDto.getStep()) {
                announceDto.setText(text);
                makeAnnounce(chatId, text);
                createAnnounceState.remove(telegramUserId);
            }
        }

        EventDto eventDto;
        if (createEventState.containsKey(telegramUserId)) {
            eventDto = createEventState.get(telegramUserId);

            switch (eventDto.getCurrentStep()) {
                case ASK_EVENT_NAME:
                    eventDto.setName(text);
                    eventDto.setCurrentStep(ASK_EVENT_LOCATION);
                    sendTextAnswer(ENTER_EVENT_LOCATION_MESSAGE, chatId);
                    break;
                case ASK_EVENT_LOCATION:
                    eventDto.setLocation(text);
                    eventDto.setCurrentStep(ASK_EVENT_DESCRIPTION);
                    sendTextAnswer(ENTER_EVENT_DESCRIPTION_MESSAGE, chatId);
                    break;
                case ASK_EVENT_DESCRIPTION:
                    eventDto.setDescription(text);
                    eventDto.setCurrentStep(ASK_EVENT_DATE_MESSAGE);
                    sendTextAnswer(ENTER_EVENT_DATE_MESSAGE, chatId);
                    break;
                case ASK_EVENT_DATE_MESSAGE:
                    try {
                        LocalDateTime date = LocalDateTime.parse(text, dateTimeFormat);
                        eventDto.setDate(date);
                        eventDto.setCurrentStep(ASK_EVENT_CATEGORY);
                        sendCallbackAnswer(chatId, ENTER_EVENT_CATEGORY_MESSAGE, getCategoryForCreateEventKeyboard());
                    } catch (DateTimeParseException e) {
                        eventDto.setCurrentStep(ASK_EVENT_DATE_MESSAGE);
                        sendTextAnswer(WRONG_EVENT_DATE_FORMAT, chatId);
                    }
                    break;
            }
        }

        TextCommand textCommand = TextCommand.fromValue(text);
        if (START.equals(textCommand)) {
            output = START_MESSAGE;
            sendCallbackAnswer(chatId, output, getMainMenuKeyboard(isAdmin(chatId)));
        }
//        }  else {
//            output = UNKNOWN_MESSAGE;
//            sendTextAnswer(output, chatId);
//        }
    }


    @Override
    public void processCallbackMessage(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String callbackData = callbackQuery.getData();

        String output;
        InlineKeyboardMarkup markup;
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        if (FAVORITE.getCommandValue().equals(callbackData)) {
            Pair<String, InlineKeyboardMarkup> pair = getFavoriteEvent(update, 0);
            output = pair.getFirst();
            markup = pair.getSecond();

            sendEditMessageCallbackAnswer(chatId, messageId, output, markup);
        } else if (callbackData.startsWith(FAVORITE_PAGE.getCommandValue())) {
            String[] parts = callbackData.split("_PAGE_");
            int page = Integer.parseInt(parts[1]);

            Pair<String, InlineKeyboardMarkup> pair = getFavoriteEvent(update, page);
            output = pair.getFirst();
            markup = pair.getSecond();

            sendEditMessageCallbackAnswer(chatId, messageId, output, markup);
        } else if (callbackData.startsWith(DETAILS.getCommandValue())) {
            String[] parts = callbackData.split("_PAGE_");
            UUID eventId = UUID.fromString(parts[0].replace("DETAILS_", ""));
            int currentPage = Integer.parseInt(parts[1].split("_")[0]);
            String context = parts[1].split("_")[1];

            Pair<String, InlineKeyboardMarkup> pair = getDetails(eventId, currentPage, context);
            output = pair.getFirst();
            markup = pair.getSecond();

            addEventToViewList(update, eventId);

            sendEditMessageCallbackAnswer(chatId, messageId, output, markup);
        } else if (FIND_BY_CATEGORY.getCommandValue().equals(callbackData)) {
            Pair<String, InlineKeyboardMarkup> pair = getCategories();
            output = pair.getFirst();
            markup = pair.getSecond();

            sendEditMessageCallbackAnswer(chatId, messageId, output, markup);
        } else if (callbackData.startsWith(CHOOSE_CATEGORY.getCommandValue()) && !callbackData.contains("_PAGE_")) {
            EventCategory selectedCategory = EventCategory.valueOf(callbackData.replace(CHOOSE_CATEGORY.getCommandValue(), ""));

            Pair<String, InlineKeyboardMarkup> eventCard = getEventCard(selectedCategory, 0, isAdmin(chatId));
            output = eventCard.getFirst();
            markup = eventCard.getSecond();

            sendEditMessageCallbackAnswer(chatId, callbackQuery.getMessage().getMessageId(), output, markup);
        } else if (callbackData.startsWith(CHOOSE_CATEGORY.getCommandValue()) && callbackData.contains("_PAGE_")) {
            String[] parts = callbackData.split("_PAGE_");
            EventCategory selectedCategory = EventCategory.valueOf(parts[0].replace(CHOOSE_CATEGORY.getCommandValue(), ""));
            int page = Integer.parseInt(parts[1]);

            Pair<String, InlineKeyboardMarkup> event = getEventCard(selectedCategory, page, isAdmin(chatId));
            output = event.getFirst();
            markup = event.getSecond();

            sendEditMessageCallbackAnswer(chatId, messageId, output, markup);
        } else if (callbackData.startsWith(ADD_TO_FAVORITE.getCommandValue())) {
            UUID eventId = UUID.fromString(callbackData.replace(ADD_TO_FAVORITE.getCommandValue(), ""));
            addEventToFavoriteList(update, eventId);
        } else if (FIND_BY_VIEWS.getCommandValue().equals(callbackData)) {
            Set<EventCategory> topThreeCategories = getEventCategoriesByViews(update);

            Pair<String, InlineKeyboardMarkup> eventCards = getEventCardsForCategories(topThreeCategories, update, 0, isAdmin(chatId));
            output = eventCards.getFirst();
            markup = eventCards.getSecond();

            sendEditMessageCallbackAnswer(chatId, messageId, output, markup);
        } else if (callbackData.startsWith(NOT_VIEWED_EVENT.getCommandValue())) {
            Set<EventCategory> topThreeCategories = getEventCategoriesByViews(update);
            String[] parts = callbackData.split("_PAGE_");
            int page = Integer.parseInt(parts[1]);

            Pair<String, InlineKeyboardMarkup> eventCards = getEventCardsForCategories(topThreeCategories, update, page, isAdmin(chatId));
            output = eventCards.getFirst();
            markup = eventCards.getSecond();

            sendEditMessageCallbackAnswer(chatId, messageId, output, markup);
        } else if (FIND_SOMETHING.getCommandValue().equals(callbackData)) {
            Pair<String, InlineKeyboardMarkup> randomEvent = getSomethingEvent(isAdmin(chatId));

            output = randomEvent.getFirst();
            markup = randomEvent.getSecond();

            sendEditMessageCallbackAnswer(chatId, messageId, output, markup);
        } else if (INFO.getCommandValue().startsWith(callbackData)) {
            Pair<String, InlineKeyboardMarkup> pair = getInfo();
            output = pair.getFirst();
            markup = pair.getSecond();
            sendEditMessageCallbackAnswer(chatId, messageId, output, markup);
        } else if (BACK_TO_MAIN.getCommandValue().equals(callbackData)) {
            output = START_MESSAGE;
            markup = getMainMenuKeyboard(isAdmin(chatId));
            sendEditMessageCallbackAnswer(chatId, messageId, output, markup);
        } else if (callbackData.equals(RELOAD.getCommandValue())) {
            output = START_MESSAGE;
            markup = getMainMenuKeyboard(isAdmin(chatId));
            findOrSaveAppUser(update);
            sendCallbackAnswer(chatId, output, markup);
        } else if (callbackData.startsWith(DELETE_FAV.getCommandValue())) {
            UUID eventId = UUID.fromString(callbackData.replace(DELETE_FAV.getCommandValue(), ""));
            deleteFavEvent(eventId);

            sendAlertCallbackAnswer(update.getCallbackQuery().getId(), SUCCESS_DELETE_FROM_FAVORITE_MESSAGE);
            sendEditMessageCallbackAnswer(chatId, messageId, START_MESSAGE, getMainMenuKeyboard(isAdmin(chatId)));
        } else if (callbackData.equals(CREATE_EVENT.getCommandValue())) {
            EventDto eventDto = new EventDto();
            eventDto.setCurrentStep(ASK_EVENT_NAME);
            createEventState.put(chatId, eventDto);

            sendEditMessageCallbackAnswer(chatId, messageId, ENTER_EVENT_NAME_MESSAGE, cancelCreateEventKeyboard());
        } else if (callbackData.equals(CANCEL_CREATING_EVENT.getCommandValue())) {
            createEventState.remove(chatId);
            sendEditMessageCallbackAnswer(chatId, messageId, START_MESSAGE, getMainMenuKeyboard(isAdmin(chatId)));
        } else if (callbackData.startsWith(CREATE_EVENT.getCommandValue())) {
            EventCategory category = EventCategory.valueOf(callbackData.replace(CREATE_EVENT + "_CATEGORY_", ""));
            EventDto eventDto = createEventState.get(chatId);
            eventDto.setCategory(category);
            eventDto.setCurrentStep(CONFIRM);

             output = String.format(DETAILS_EVENT_MESSAGE,
                     eventDto.getName(),
                     eventDto.getLocation(),
                     eventDto.getDate().format(dateFormat),
                     eventDto.getDate().format(timeFormat),
                     eventDto.getCategory().getCategoryName(),
                     eventDto.getDescription());

            sendEditMessageCallbackAnswer(chatId, messageId, output, confirmCreateEvent());
        } else if (callbackData.equals(CONFIRM_CREATE_EVENT.getCommandValue())) {
            EventDto eventDto = createEventState.get(chatId);
            createEvent(eventDto);
            createEventState.remove(chatId);

            sendAlertCallbackAnswer(update.getCallbackQuery().getId(), SUCCESS_CREATING_EVENT_MESSAGE);
            sendEditMessageCallbackAnswer(chatId, messageId, START_MESSAGE, getMainMenuKeyboard(isAdmin(chatId)));
        } else if (callbackData.equals(USERS_ANALYTICS.getCommandValue())) {
            Pair<String, InlineKeyboardMarkup> pair = getAnalytics();
            output = pair.getFirst();
            markup = pair.getSecond();

            sendEditMessageCallbackAnswer(chatId, messageId, output, markup);
        } else if (callbackData.equals(ANNOUNCE.getCommandValue())) {
            AnnounceDto announceDto = new AnnounceDto();
            announceDto.setStep(ASK_ANNOUNCEMENT_TEXT);
            createAnnounceState.put(chatId, announceDto);

            sendTextAnswer(ENTER_ANNOUNCE_TEXT, chatId);
        } else if (callbackData.equals(PARSE_EVENT.getCommandValue())) {
            output = eventScheduler.getEvents();

            sendAlertCallbackAnswer(update.getCallbackQuery().getId(), START_PROCESS);
            sendEditMessageCallbackAnswer(chatId, messageId, output, goToMainKeyboard());
        } else if (callbackData.startsWith(DELETE_EVENT.getCommandValue())) {
            UUID eventId = UUID.fromString(callbackData.replace(DELETE_EVENT.getCommandValue(), ""));
            deleteEvent(eventId);

            sendAlertCallbackAnswer(update.getCallbackQuery().getId(), SUCCESS_DELETE_EVENT_MESSAGE);
            sendEditMessageCallbackAnswer(chatId, messageId, START_MESSAGE, getMainMenuKeyboard(isAdmin(chatId)));
        } else {
            log.warn("Unknown callback data: " + callbackData);
        }
    }

    @Transactional
    public void deleteEvent(UUID eventId) {
        viewDAO.deleteByEventId(eventId);
        favoriteDAO.deleteByEventId(eventId);
        eventDAO.deleteById(eventId);
    }

    private void makeAnnounce(Long adminChatId, String output) {
        List<Long> chatIds = appUserDAO.findAll().stream().map(AppUser::getChatId).collect(Collectors.toList());
        for (Long chatId : chatIds) {
            if (!adminChatId.equals(chatId)) {
                sendTextAnswer(output, chatId);
            }
        }

        chatIds.remove(adminChatId);
        sendTextAnswer(String.format(NOTIFICATION_MESSAGE, chatIds.size(), chatIds), adminChatId);
    }

    private Pair<String, InlineKeyboardMarkup> getAnalytics() {
        List<AppUser> appUsers = appUserDAO.findAll();
        List<Event> events = eventDAO.findAll();

        String output = String.format(
                ANALYTICS_MESSAGE,
                appUsers.size(),
                appUsers.stream().filter(u -> u.getRegistrationDate().isAfter(LocalDate.now().atStartOfDay())
                        && u.getRegistrationDate().isBefore(LocalDate.now().atStartOfDay().plusDays(1))).count(),

                appUsers.stream().filter(u -> u.getRegistrationDate().isAfter(LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay())
                        && u.getRegistrationDate().isBefore(LocalDate.now().with(DayOfWeek.MONDAY).plusDays(7).atStartOfDay())).count(),

                appUsers.stream().filter(u -> u.getRegistrationDate().isAfter(LocalDate.now().withDayOfMonth(1).atStartOfDay())
                        && u.getRegistrationDate().isBefore(LocalDate.now().withDayOfMonth(1).atStartOfDay().plusMonths(1))).count(),

                events.size(),
                events.stream().filter(e -> e.getCategory().equals(EventCategory.CONCERT)).count(),
                events.stream().filter(e -> e.getCategory().equals(EventCategory.THEATER)).count(),
                events.stream().filter(e -> e.getCategory().equals(EventCategory.CINEMA)).count(),
                events.stream().filter(e -> e.getCategory().equals(EventCategory.EXHIBITION)).count(),
                events.stream().filter(e -> e.getCategory().equals(EventCategory.BUSINESS)).count(),
                events.stream().filter(e -> e.getCategory().equals(EventCategory.TOUR)).count()
                );

        InlineKeyboardMarkup markup = getAnalyticsKeyboardMarkup();
        return new Pair<>(output, markup);
    }

    private void createEvent(EventDto eventDto) {
        eventDAO.save(Event.builder()
                .name(eventDto.getName())
                .location(eventDto.getLocation())
                .description(eventDto.getDescription())
                .startDate(LocalDate.parse(eventDto.getDate().format(dateFormat)))
                .startTime(LocalTime.parse(eventDto.getDate().format(timeFormat)))
                .category(eventDto.getCategory())
                .build());
    }

    private void deleteFavEvent(UUID eventId) {
        Optional<Favorite> optional = favoriteDAO.findByEventId(eventId);

        if (optional.isPresent()) {
            AppUser appUser = optional.get().getAppUser();

            appUser.getFavorites().remove(optional.get());
            appUserDAO.save(appUser);
        }
    }

    private Pair<String, InlineKeyboardMarkup> getEventCardsForCategories(Set<EventCategory> categories, Update update, int page, boolean isAdmin) {
        User telegramUser = update.getCallbackQuery().getFrom();
        AppUser appUser = appUserDAO.findByTelegramUserId(telegramUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        List<Event> resultEvents = new ArrayList<>();

        Pageable pageable = PageRequest.of(0, 1);

        for (EventCategory category : categories) {
            Page<Event> events = eventDAO.findNotViewedEventsByCategory(category, appUser.getId(), pageable);
            events.stream().findFirst().ifPresent(resultEvents::add);
        }


        return getNotViewedEventCard(resultEvents, page, isAdmin);
    }

    private Pair<String, InlineKeyboardMarkup> getNotViewedEventCard(List<Event> resultEvents, int page, boolean isAdmin) {
        if (resultEvents.isEmpty()) {
            return new Pair<>(CANNOT_FIND_EVENT_BY_VIEWS, getMainAndFindByCategoryActionKeyboard());
        }

        Event event = resultEvents.get(page);
        String output = formatEventMessage(event, EVENT_MESSAGE, page, false, null);

        InlineKeyboardMarkup markup = getNotViewedEventNavigationKeyboard(event, page, resultEvents.size(), isAdmin);
        return new Pair<>(output, markup);

    }

    private Pair<String, InlineKeyboardMarkup> getDetails(UUID eventId, int currentPage, String context) {
        Event event = eventDAO.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Мероприятие не найдено"));

        String output = formatEventMessage(event, DETAILS_EVENT_MESSAGE, null, true, null);

        InlineKeyboardMarkup markup = getEventDetailsNavigationKeyboard(event, currentPage, context);
        return new Pair<>(output, markup);
    }

    private void addEventToViewList(Update update, UUID eventId) {
        Event event = eventDAO.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Мероприятие не найдено"));

        User telegramUser = update.getCallbackQuery().getFrom();
        AppUser appUser = appUserDAO.findByTelegramUserId(telegramUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        if (!isEventInViewList(event, appUser)) {
            View view = View.builder()
                    .appUser(appUser)
                    .event(event)
                    .createdAt(LocalDateTime.now())
                    .build();

            appUser.getViews().add(view);

            appUserDAO.save(appUser);
            log.info("Мероприятие добавлено в список просмотренных");
        }
    }

    private boolean isEventInViewList(Event event, AppUser appUser) {
        for (View view : appUser.getViews()) {
            if (view.getEvent().equals(event)) {
                return true;
            }
        }

        return false;
    }

    public void addEventToFavoriteList(Update update, UUID eventId) {
        Event event = eventDAO.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Мероприятие не найдено"));

        User telegramUser = update.getCallbackQuery().getFrom();
        AppUser appUser = appUserDAO.findByTelegramUserId(telegramUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        if (isEventInFavoriteList(event, appUser)) {
            sendAlertCallbackAnswer(update.getCallbackQuery().getId(), FAVORITE_EVENT_IS_EXIST);
        } else {
            Favorite favorite = Favorite.builder()
                    .appUser(appUser)
                    .event(event)
                    .createdAt(LocalDateTime.now())
                    .build();

            appUser.getFavorites().add(favorite);

            appUserDAO.save(appUser);
            sendAlertCallbackAnswer(update.getCallbackQuery().getId(), SUCCESS_ADDING_TO_FAVORITE);
        }
    }

    private boolean isEventInFavoriteList(Event event, AppUser appUser) {
        for (Favorite favorite : appUser.getFavorites()) {
            if (favorite.getEvent().equals(event)) {
                return true;
            }
        }

        return false;
    }

    public Pair<String, InlineKeyboardMarkup> getEventCard(EventCategory category, int page, boolean isAdmin) {
        Page<Event> eventPage = searchEventsByCategory(category, page);

        if (eventPage.isEmpty()) {
            return new Pair<>("🚫 В этой категории пока нет мероприятий.", goToMainKeyboard());
        }

        Event event = eventPage.getContent().get(0);
        String output = formatEventMessage(event, EVENT_MESSAGE, page, false, null);

        InlineKeyboardMarkup markup = getEventNavigationKeyboard(event, category, page, eventPage.getTotalPages(), isAdmin);

        return new Pair<>(output, markup);
    }

    private Page<Event> searchEventsByCategory(EventCategory category, int page) {
        Pageable pageable = PageRequest.of(page, 1);
        return eventDAO.findEventsByCategory(category, pageable);
    }

    private Pair<String, InlineKeyboardMarkup> getSomethingEvent(boolean isAdmin) {
        long total = eventDAO.count();
        int randomIndex = new Random().nextInt((int) total);

        PageRequest pageRequest = PageRequest.of(randomIndex, 1);
        Page<Event> eventPage = eventDAO.findAll(pageRequest);

        Event randomEvent = eventPage.getContent().isEmpty() ? null : eventPage.getContent().get(0);
        String output = formatEventMessage(randomEvent, RANDOM_EVENT_MESSAGE, null, false, null);

        InlineKeyboardMarkup markup = getRandomEventNavigationKeyboard(randomEvent, randomIndex, isAdmin);
        return new Pair<>(output, markup);
    }

    private Set<EventCategory> getEventCategoriesByViews(Update update) {
        User telegramUser = update.getCallbackQuery().getFrom();
        AppUser appUser = appUserDAO.findByTelegramUserId(telegramUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        return getTopThreeCategories(appUser);
    }

    private Set<EventCategory> getTopThreeCategories(AppUser appUser) {
        HashMap<EventCategory, Integer> countViewsByCategory = new HashMap<>();
        List<View> viewList = appUser.getViews();

        for (View view : viewList) {
            countViewsByCategory.put(view.getEvent().getCategory(), countViewsByCategory.getOrDefault(view.getEvent().getCategory(), 0) + 1);
        }

        List<Map.Entry<EventCategory, Integer>> sortedList = new ArrayList<>(countViewsByCategory.entrySet());
        sortedList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        Set<EventCategory> topThreeCategories = new HashSet<>();
        for (int i = 0; i < 3 && i < sortedList.size(); i++) {
            topThreeCategories.add(sortedList.get(i).getKey());
        }

        return topThreeCategories;
    }

    private Pair<String, InlineKeyboardMarkup> getFavoriteEvent(Update update, int page) {
        User telegramUser = update.getCallbackQuery().getFrom();
        AppUser appUser = appUserDAO.findByTelegramUserId(telegramUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        Page<Favorite> favoritePage = searchFavoriteEvents(appUser, page);
        if (favoritePage.isEmpty()) {
            return new Pair<>("🚫 У вас нет избранных мероприятий.", goToMainKeyboard());
        }

        Event event = favoritePage.getContent().get(0).getEvent();
        String output = formatEventMessage(event, EVENT_MESSAGE, page, false, "\t<b>Избранные мероприятия</b>\n\n");

        InlineKeyboardMarkup markup = getFavoriteEventNavigationKeyboard(event ,page, favoritePage.getTotalPages());
        return new Pair<>(output, markup);
    }

    private String getGoogleMapPosition(String lat, String lon) {
        if (!lat.isEmpty() && !lon.isEmpty()) {
            String GEOPOSITION_LINK = "https://www.google.com/maps?q=%s,%s";
            return String.format(GEOPOSITION_LINK, lat, lon);
        }

        return "-";
    }

    private Page<Favorite> searchFavoriteEvents(AppUser appUser, int page) {
        Pageable pageable = PageRequest.of(page, 1);
        return favoriteDAO.findByAppUser(appUser, pageable);
    }

    private Pair<String, InlineKeyboardMarkup> getCategories() {
        return new Pair<>(CATEGORY_MESSAGE, getCategoryKeyboard());
    }

    private Pair<String, InlineKeyboardMarkup> getInfo() {
        return new Pair<>(INFO_BOT_MESSAGE, getInfoBotKeyboard());
    }

    private void findOrSaveAppUser(Update update) {
        User telegramUser;
        Long chatId;

        if (update.getMessage() != null) {
            telegramUser = update.getMessage().getFrom();
            chatId = update.getMessage().getChatId();
        } else {
            telegramUser = update.getCallbackQuery().getFrom();
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }

        Optional<AppUser> optional = appUserDAO.findByTelegramUserId(telegramUser.getId());
        if (optional.isEmpty()) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .chatId(chatId)
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .favorites(new ArrayList<>())
                    .build();

            appUserDAO.save(transientAppUser);
        }
    }

    private void sendTextAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }

    private void sendCallbackAnswer(Long chatId, String output, InlineKeyboardMarkup markup) {
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

    private void sendAlertCallbackAnswer(String id, String message) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(id);
        answerCallbackQuery.setText(message);
        answerCallbackQuery.setShowAlert(true);

        producerService.produceAlertAnswer(answerCallbackQuery);
    }

    private boolean isAdmin(Long telegramUserId) {
        return adminList.contains(telegramUserId);
    }

    public String formatEventMessage(Event event, String template, Integer page,boolean includeDescription,String titlePrefix) {
        List<Object> args = new ArrayList<>();

        if (page != null) {
            args.add(page + 1);
        }

        args.add(event.getName());
        args.add(event.getLocation().isEmpty() ? "-" : event.getLocation());
        args.add(event.getStartDate() != null ? event.getStartDate().format(dateFormat) : "-");
        args.add(event.getStartTime() != null ? event.getStartTime().format(timeFormat) : "-");
        args.add(event.getCategory().getCategoryName());
        args.add(event.getSiteUrl());
        args.add(getGoogleMapPosition(event.getLat(), event.getLon()));

        if (includeDescription) {
            args.add(event.getDescription() != null ? event.getDescription() : "");
        }

        String formatted = String.format(template, args.toArray());

        if (titlePrefix != null && !titlePrefix.isEmpty()) {
            formatted = titlePrefix + formatted;
        }

        return formatted;
    }

}
