
package com.dhanush.ticketing;

import com.dhanush.ticketing.messaging.BookingConfirmedEvent;
import com.dhanush.ticketing.outbox.OutboxEvent;
import com.dhanush.ticketing.outbox.OutboxEventPublisher;
import com.dhanush.ticketing.outbox.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test", "outbox-test"})
class OutboxEventPublisherIntegrationTest extends SharedTestContainers {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publishesOutboxEventAndMarksItPublished() throws Exception {
        UUID bookingId = UUID.randomUUID();

        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID(),
                "BOOKING",
                bookingId,
                "BOOKING_CONFIRMED",
                objectMapper.writeValueAsString(
                        new BookingConfirmedEvent(bookingId)
                ),
                OffsetDateTime.now(ZoneOffset.UTC)
        );

        outboxEventRepository.saveAndFlush(event);

        OutboxEventPublisher publisher = new OutboxEventPublisher(
                outboxEventRepository,
                rabbitTemplate,
                objectMapper,
                "booking.exchange",
                "booking.confirmed"
        );

        // Publish the outbox event to RabbitMQ.
        publisher.publishUnpublishedEvents();

        OutboxEvent updatedEvent = outboxEventRepository
                .findById(event.getId())
                .orElseThrow();

        assertNotNull(
                updatedEvent.getPublishedAt(),
                "The outbox event should be marked as published"
        );

        // Read the message directly from the queue before the consumer
        // can process it. This assertion verifies the published payload.
        Message received = rabbitTemplate.receive(
                "ticket.generation.queue",
                5000
        );

        assertNotNull(received, "Expected a message in the ticket queue");

        String payload = new String(
                received.getBody(),
                StandardCharsets.UTF_8
        );

        BookingConfirmedEvent receivedEvent = objectMapper.readValue(
                payload,
                BookingConfirmedEvent.class
        );

        assertEquals(bookingId, receivedEvent.bookingId());
    }
}
