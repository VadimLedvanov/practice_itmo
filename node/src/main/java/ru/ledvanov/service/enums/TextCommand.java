package ru.ledvanov.service.enums;

public enum TextCommand {
    HELP("/help"),
    START("/start");
    private final String value;
    TextCommand(String cmd) {
        this.value = cmd;
    }

    @Override
    public String toString() {
        return value;
    }

    public static TextCommand fromValue(String v) {
        for (TextCommand c : TextCommand.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return null;
    }
}
