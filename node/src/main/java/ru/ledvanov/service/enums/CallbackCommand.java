package ru.ledvanov.service.enums;

public enum CallbackCommand {
    FAVORITE("favorite_event_list"),
    FAVORITE_PAGE("FAVORITE_PAGE_"),
    ADD_TO_FAVORITE("add_to_favorite_"),
    DETAILS("DETAILS_"),
    FIND_BY_CATEGORY("find_by_category"),
    CHOOSE_CATEGORY("CATEGORY_"),
    FIND_BY_VIEWS("find_by_views"),
    NOT_VIEWED_EVENT("NOT_VIEWED_EVENT_PAGE_"),
    FIND_SOMETHING("find_something"),
    INFO("info_bot"),
    BACK_TO_MAIN("back_to_main"),
    RELOAD("RELOAD"),
    DELETE_FAV("DELETE_FAV_"),
    DELETE_EVENT("DELETE_EVENT_"),
    CREATE_EVENT("CREATE_EVENT"),
    USERS_ANALYTICS("USERS_ANALYTICS"),
    ANNOUNCE("ANNOUNCE"),
    CANCEL_CREATING_EVENT("CANCEL_CREATING_EVENT"),
    CONFIRM_CREATE_EVENT("CONFIRM_CREATE_EVENT"),
    PARSE_EVENT("PARSE_EVENT");
    private final String value;
    CallbackCommand(String cmd) {
        this.value = cmd;
    }

    public String getCommandValue() {
        return this.value;
    }
}
