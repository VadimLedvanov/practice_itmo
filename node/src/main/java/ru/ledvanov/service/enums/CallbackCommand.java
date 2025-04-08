package ru.ledvanov.service.enums;

public enum CallbackCommand {
    FAVORITE("favorite_event_list"),
    FAVORITE_PAGE("FAVORITE_PAGE_"),
    ADD_TO_FAVORITE("add_to_favorite_"),
    DETAILS("DETAILS_"),
    FIND_BY_CATEGORY("find_by_category"),
    CHOOSE_CATEGORY("CATEGORY_"),
    FIND_BY_VIEWS("find_by_views"),
    FIND_SOMETHING("find_something"),
    INFO("info_bot"),
    BACK_TO_MAIN("back_to_main");
    private String value;
    CallbackCommand(String cmd) {
        this.value = cmd;
    }

    public static CallbackCommand fromValue(String v) {
        for (CallbackCommand c : CallbackCommand.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return null;
    }

    public String getCommandValue() {
        return this.value;
    }
}
