package com.dhanush.ticketing.booking;

import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class BookingCancellationService {

    private final BookingCancellationTransactionService transactionService;
    private final RedissonClient redissonClient;

    public BookingCancellationService(
            BookingCancellationTransactionService transactionService,
            RedissonClient redissonClient) {
        this.transactionService = transactionService;
        this.redissonClient = redissonClient;
    }

    public Booking cancelBooking(UUID bookingId, UUID userId) {
        Booking booking = transactionService.findBooking(bookingId);
        UUID eventId = booking.getEvent().getId();
        UUID seatId = booking.getEventSeat().getSeat().getId();
        RLock lock = redissonClient.getLock(
                "seat-lock:event:" + eventId + ":seat:" + seatId);

        try {
            boolean acquired = lock.tryLock(2, 5, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException(
                        "Seat is currently being processed. Please try again.");
            }

            Booking cancelled = transactionService.cancelBooking(
                    bookingId,
                    userId);

            RBucket<String> reservationBucket = redissonClient.getBucket(
                    "seat-reservation:event:" + eventId + ":seat:" + seatId);
            reservationBucket.delete();
            return cancelled;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Booking cancellation was interrupted",
                    exception);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
