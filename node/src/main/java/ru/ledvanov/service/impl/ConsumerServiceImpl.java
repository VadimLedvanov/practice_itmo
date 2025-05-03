package ru.ledvanov.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.ledvanov.service.ConsumerService;
import ru.ledvanov.service.MainService;

import static ru.ledvanov.RabbitQueue.*;
import static ru.ledvanov.exception.EntityNotFoundException.handleUserNotFoundException;
import static ru.ledvanov.factory.KeyboardFactory.reloadBotKeyboard;
import static ru.ledvanov.messages.TextMessage.SOMETHING_WRONG_MESSAGE;

@Service
@Log4j
public class ConsumerServiceImpl implements ConsumerService {
    private final MainService mainService;

    public ConsumerServiceImpl(MainService mainService) {
        this.mainService = mainService;
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
        try {
            mainService.processCallbackMessage(update);
        } catch (Exception e) {
            log.error(e);
            handleUserNotFoundException(
                    SOMETHING_WRONG_MESSAGE,
                    update.getCallbackQuery().getMessage().getChatId(),
                    reloadBotKeyboard()
            );
        }
    }
}