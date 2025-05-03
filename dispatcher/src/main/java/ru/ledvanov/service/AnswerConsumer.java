package ru.ledvanov.service;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

public interface AnswerConsumer {
    void consume(SendMessage sendMessage);
    void consume(EditMessageText editMessage);
    void consume(AnswerCallbackQuery alert);
}
