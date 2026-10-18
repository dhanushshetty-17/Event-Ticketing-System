package com.dhanush.ticketing.outbox;

import com.dhanush.ticketing.messaging.BookingConfirmedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxEventService(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {

        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void createBookingConfirmedEvent(UUID bookingId) {

        BookingConfirmedEvent event = new BookingConfirmedEvent(bookingId);

        final String payload;

        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize booking confirmation event",
                    exception
            );
        }

        OutboxEvent outboxEvent = new OutboxEvent(
                UUID.randomUUID(),
                "BOOKING",
                bookingId,
                "BOOKING_CONFIRMED",
                payload,
                OffsetDateTime.now(ZoneOffset.UTC)
        );

        outboxEventRepository.save(outboxEvent);
    }
}