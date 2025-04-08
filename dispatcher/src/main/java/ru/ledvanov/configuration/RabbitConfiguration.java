package ru.ledvanov.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static ru.ledvanov.RabbitQueue.*;

@Configuration
public class RabbitConfiguration {
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue textMessageQueue() {
        return new Queue(TEXT_MESSAGE_UPDATE);
    }

    @Bean
    public Queue answerMessageQueue() {
        return new Queue(ANSWER_MESSAGE);
    }
    @Bean
    public Queue answerEditedMessageQueue() {
        return new Queue(ANSWER_EDITED_MESSAGE);
    }

    @Bean
    public Queue callbackMessageQueue() {
        return new Queue(CALLBACK_MESSAGE_UPDATE);
    }

    @Bean
    public Queue callbackEditedMessageQueue() {
        return new Queue(CALLBACK_EDITED_MESSAGE_UPDATE);
    }
}
