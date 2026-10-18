
package com.dhanush.ticketing.outbox;

import com.dhanush.ticketing.messaging.BookingConfirmedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Profile;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Profile("!test")
@Component
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String exchange;
    private final String routingKey;

    public OutboxEventPublisher(
            OutboxEventRepository outboxEventRepository,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${rabbitmq.exchange}") String exchange,
            @Value("${rabbitmq.booking-confirmed-routing-key}") String routingKey) {

        this.outboxEventRepository = outboxEventRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishUnpublishedEvents() {

        List<OutboxEvent> events =
                outboxEventRepository.findUnpublishedEventsForUpdate();

        for (OutboxEvent event : events) {

            if (!"BOOKING_CONFIRMED".equals(event.getEventType())) {
                continue;
            }

            final BookingConfirmedEvent payload;

            try {
                payload = objectMapper.readValue(
                        event.getPayload(),
                        BookingConfirmedEvent.class
                );
            } catch (JsonProcessingException exception) {
                throw new IllegalStateException(
                        "Failed to deserialize outbox event: "
                                + event.getId(),
                        exception
                );
            }

            CorrelationData correlationData =
                    new CorrelationData(UUID.randomUUID().toString());

            try {
                rabbitTemplate.convertAndSend(
                        exchange,
                        routingKey,
                        payload,
                        correlationData
                );

                CorrelationData.Confirm confirm =
                        correlationData.getFuture().get(5, TimeUnit.SECONDS);

                if (!confirm.isAck()) {
                    throw new IllegalStateException(
                            "RabbitMQ rejected outbox event "
                                    + event.getId() + ": "
                                    + confirm.getReason()
                    );
                }

                if (correlationData.getReturned() != null) {
                    throw new IllegalStateException(
                            "RabbitMQ returned outbox event "
                                    + event.getId()
                                    + " because it could not be routed"
                    );
                }

                event.setPublishedAt(
                        OffsetDateTime.now(ZoneOffset.UTC)
                );

                outboxEventRepository.save(event);

            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(
                        "Interrupted while publishing outbox event: "
                                + event.getId(),
                        exception
                );
            } catch (TimeoutException | AmqpException exception) {
                throw new IllegalStateException(
                        "Failed to publish outbox event: "
                                + event.getId(),
                        exception
                );
            } catch (java.util.concurrent.ExecutionException exception) {
                throw new IllegalStateException(
                        "Could not confirm outbox event: "
                                + event.getId(),
                        exception
                );
            }
        }
    }
}
