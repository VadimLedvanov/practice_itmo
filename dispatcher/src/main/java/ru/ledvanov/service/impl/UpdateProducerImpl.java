package ru.ledvanov.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.ledvanov.service.UpdateProducer;

@Service
@Log4j
public class UpdateProducerImpl implements UpdateProducer {
    private final RabbitTemplate rabbitTemplate;

    public UpdateProducerImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void produce(String rabbitQueue, Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            log.debug("produce message: " + update.getMessage().getText());
        } else if (update.hasCallbackQuery()) {
            log.debug("produce callback query: " + update.getCallbackQuery().getData());
        }
        rabbitTemplate.convertAndSend(rabbitQueue, update);
    }
}