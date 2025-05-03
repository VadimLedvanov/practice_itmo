package ru.ledvanov.service.impl;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import ru.ledvanov.controller.UpdateProcessor;
import ru.ledvanov.service.AnswerConsumer;

import static ru.ledvanov.RabbitQueue.*;

@Service
public class AnswerConsumerImpl implements AnswerConsumer {
    private final UpdateProcessor updateProcessor;

    public AnswerConsumerImpl(UpdateProcessor updateProcessor) {
        this.updateProcessor = updateProcessor;
    }

    @Override
    @RabbitListener(queues = ANSWER_MESSAGE)
    public void consume(SendMessage sendMessage) {
        updateProcessor.setView(sendMessage);
    }

    @Override
    @RabbitListener(queues = ANSWER_EDITED_MESSAGE)
    public void consume(EditMessageText editMessage) {
        updateProcessor.setView(editMessage);
    }

    @Override
    @RabbitListener(queues = ANSWER_ALERT)
    public void consume(AnswerCallbackQuery alert) {
        updateProcessor.setView(alert);
    }
}