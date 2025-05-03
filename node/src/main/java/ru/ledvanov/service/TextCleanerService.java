package ru.ledvanov.service;


public class TextCleanerService {
    private static String removeEmojis(String text) {
        return text.replaceAll("[\\p{So}\\p{Cn}]", "");
    }

    private static String stripHtmlTags(String html) {
        return html.replaceAll("<[^>]*>", "");
    }

    private static String capitalizeFirstLetter(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static String clean(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }

        return capitalizeFirstLetter(stripHtmlTags(removeEmojis(raw).trim()));
    }
}
