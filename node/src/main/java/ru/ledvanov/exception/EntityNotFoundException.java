package ru.ledvanov.exception;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.ledvanov.service.ProducerService;

@Service
public class EntityNotFoundException {
    private static ProducerService producerService;

    public EntityNotFoundException(ProducerService producerService) {
        EntityNotFoundException.producerService = producerService;
    }

    public static void handleUserNotFoundException(String output, Long chatId, InlineKeyboardMarkup markup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(output);
        message.setReplyMarkup(markup);
        message.setParseMode("HTML");

        producerService.producerAnswer(message);
    }
}
