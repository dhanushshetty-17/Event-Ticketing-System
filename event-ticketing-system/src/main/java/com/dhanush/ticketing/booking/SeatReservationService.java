package com.dhanush.ticketing.booking;

import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SeatReservationService {

    private static final long RESERVATION_TTL_MINUTES = 10;

    private final SeatReservationTransactionService transactionService;
    private final RedissonClient redissonClient;

    public SeatReservationService(
            SeatReservationTransactionService transactionService,
            RedissonClient redissonClient) {

        this.transactionService = transactionService;
        this.redissonClient = redissonClient;
    }

    public Booking reserveSeat(UUID userId, UUID eventId, UUID seatId) {

        String lockKey = "seat-lock:event:" + eventId + ":seat:" + seatId;

        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(2, 5, TimeUnit.SECONDS);

            if (!acquired) {
                throw new IllegalStateException(
                        "Seat is currently being processed. Please try again.");
            }

            Booking booking =
                    transactionService.reserveSeat(userId, eventId, seatId);

            String reservationKey =
                    "seat-reservation:event:" + eventId + ":seat:" + seatId;

            RBucket<String> reservationBucket =
                    redissonClient.getBucket(reservationKey);

            reservationBucket.set(
                    booking.getId().toString(),
                    RESERVATION_TTL_MINUTES,
                    TimeUnit.MINUTES
            );

            return booking;

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Reservation was interrupted",
                    exception
            );

        } finally {

            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}