package ru.ledvanov.service.impl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import ru.ledvanov.service.ProducerService;

import static ru.ledvanov.RabbitQueue.*;

@Service
public class ProducerServiceImpl implements ProducerService {
    private final RabbitTemplate rabbitTemplate;

    public ProducerServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void producerAnswer(SendMessage sendMessage) {
        rabbitTemplate.convertAndSend(ANSWER_MESSAGE, sendMessage);
    }

    @Override
    public void produceEditedAnswer(EditMessageText editMessage) {
        rabbitTemplate.convertAndSend(ANSWER_EDITED_MESSAGE, editMessage);
    }

    @Override
    public void produceAlertAnswer(AnswerCallbackQuery answerCallbackQuery) {
        rabbitTemplate.convertAndSend(ANSWER_ALERT, answerCallbackQuery);
    }
}