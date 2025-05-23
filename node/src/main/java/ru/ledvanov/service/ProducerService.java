package ru.ledvanov.service;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

public interface ProducerService {
    void producerAnswer(SendMessage sendMessage);
    void produceEditedAnswer(EditMessageText editMessage);

    void produceAlertAnswer(AnswerCallbackQuery answerCallbackQuery);
}
