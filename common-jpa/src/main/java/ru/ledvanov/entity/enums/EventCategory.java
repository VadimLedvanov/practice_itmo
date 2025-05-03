package ru.ledvanov.entity.enums;

public enum EventCategory {
    CONCERT("🎵 Концерты"),
    THEATER("🎭 Театр"),
    CINEMA("🎬 Кино"),
    EXHIBITION("🖼 Выставки"),
    BUSINESS("💼 Бизнес-встречи"),
    TOUR("🏛️ Экскурсии");

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
