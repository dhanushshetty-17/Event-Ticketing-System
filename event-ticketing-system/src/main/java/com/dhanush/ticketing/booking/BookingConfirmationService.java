package com.dhanush.ticketing.booking;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class BookingConfirmationService {

    private final BookingConfirmationTransactionService transactionService;
    private final RedissonClient redissonClient;

    public BookingConfirmationService(
            BookingConfirmationTransactionService transactionService,
            RedissonClient redissonClient) {

        this.transactionService = transactionService;
        this.redissonClient = redissonClient;
    }

    public Booking confirmBooking(UUID bookingId, UUID userId) {

        Booking booking = transactionService.findBooking(bookingId);

        UUID eventId = booking.getEvent().getId();
        UUID seatId = booking.getEventSeat().getSeat().getId();

        String lockKey =
                "seat-lock:event:" + eventId + ":seat:" + seatId;

        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(
                    2,
                    5,
                    TimeUnit.SECONDS
            );

            if (!acquired) {
                throw new IllegalStateException(
                        "Seat is currently being processed. Please try again."
                );
            }

            Booking confirmedBooking = transactionService.confirmBooking(
                    bookingId,
                    userId
            );

            if (confirmedBooking == null) {
                throw new IllegalStateException(
                        "Booking reservation has expired");
            }

            return confirmedBooking;

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Booking confirmation was interrupted",
                    exception
            );

        } finally {

            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}