package com.dhanush.ticketing.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

    @Bean
    public DirectExchange bookingExchange() {
        return new DirectExchange("booking.exchange", true, false);
    }

    @Bean
    public Queue ticketGenerationQueue() {
        return new Queue("ticket.generation.queue", true);
    }

    @Bean
    public Binding ticketGenerationBinding(
            Queue ticketGenerationQueue,
            DirectExchange bookingExchange) {

        return BindingBuilder
                .bind(ticketGenerationQueue)
                .to(bookingExchange)
                .with("booking.confirmed");
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}