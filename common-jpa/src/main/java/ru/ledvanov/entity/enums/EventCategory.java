package ru.ledvanov.entity.enums;

public enum EventCategory {
    CONCERT("🎵 Концерты"),
    THEATER("🎭 Театр"),
    CINEMA("🎬 Кино"),
    EXHIBITION("🖼 Выставки"),
    SPORT("⚽ Спорт");

    private final String category;

    EventCategory(String category) {
        this.category = category;
    }

    public String getCategoryName() {
        return category;
    }

    @Override
    public String toString() {
        return category;
    }
}
