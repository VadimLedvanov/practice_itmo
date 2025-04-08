package ru.ledvanov.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.ledvanov.service.ConsumerService;
import ru.ledvanov.service.MainService;
import ru.ledvanov.service.ProducerService;

import static ru.ledvanov.RabbitQueue.*;

@Service
@Log4j
public class ConsumerServiceImpl implements ConsumerService {
    private final MainService mainService;
    private final ProducerService producerService;

    public ConsumerServiceImpl(MainService mainService, ProducerService producerService) {
        this.mainService = mainService;
        this.producerService = producerService;
    }

    @Override
    @RabbitListener(queues = TEXT_MESSAGE_UPDATE)
    public void consumeTextMessageUpdates(Update update) {
        log.debug("NODE: Text message is received");
        mainService.processTextMessage(update);
    }

    @Override
    @RabbitListener(queues = CALLBACK_MESSAGE_UPDATE)
    public void consumeCallbackMessageUpdates(Update update) {
        log.debug("NODE: Callback message is received");
        mainService.processCallbackMessage(update);
    }
}