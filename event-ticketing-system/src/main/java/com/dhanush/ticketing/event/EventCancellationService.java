package com.dhanush.ticketing.event;

import com.dhanush.ticketing.booking.Booking;
import com.dhanush.ticketing.booking.BookingRepository;
import com.dhanush.ticketing.booking.BookingStatus;
import com.dhanush.ticketing.ticket.TicketRepository;
import com.dhanush.ticketing.ticket.TicketStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class EventCancellationService {

    private final EventRepository eventRepository;
    private final EventSeatRepository eventSeatRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final RedissonClient redissonClient;

    public EventCancellationService(
            EventRepository eventRepository,
            EventSeatRepository eventSeatRepository,
            BookingRepository bookingRepository,
            TicketRepository ticketRepository,
            RedissonClient redissonClient) {
        this.eventRepository = eventRepository;
        this.eventSeatRepository = eventSeatRepository;
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.redissonClient = redissonClient;
    }

    @Transactional
    public Event cancelEvent(UUID eventId, UUID organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new IllegalStateException(
                    "You are not allowed to cancel this event");
        }

        if (event.getStatus() == EventStatus.CANCELLED) {
            return event;
        }

        List<EventSeat> eventSeats = eventSeatRepository.findAllByEventId(eventId);
        List<RLock> acquiredLocks = new ArrayList<>();
        try {
            for (EventSeat eventSeat : eventSeats) {
                RLock lock = redissonClient.getLock(
                        "seat-lock:event:" + eventId
                                + ":seat:" + eventSeat.getSeat().getId());
                if (!lock.tryLock(2, 5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException(
                            "A seat is currently being processed. Please try again.");
                }
                acquiredLocks.add(lock);
            }

            event.setStatus(EventStatus.CANCELLED);
            eventSeats.forEach(eventSeat -> {
                eventSeat.setStatus(EventSeatStatus.DISABLED);
                eventSeat.setReservedBy(null);
                eventSeat.setReservedAt(null);
                eventSeat.setReservationExpiresAt(null);
                eventSeatRepository.save(eventSeat);
                RBucket<String> reservationBucket = redissonClient.getBucket(
                        "seat-reservation:event:" + eventId
                                + ":seat:" + eventSeat.getSeat().getId());
                reservationBucket.delete();
            });

            for (Booking booking : bookingRepository.findAllByEvent_Id(eventId)) {
                if (booking.getStatus() == BookingStatus.PENDING
                        || booking.getStatus() == BookingStatus.CONFIRMED) {
                    booking.setStatus(BookingStatus.CANCELLED);
                    bookingRepository.save(booking);
                    ticketRepository.findWithDetailsByBookingId(booking.getId())
                            .ifPresent(ticket -> {
                                ticket.setStatus(TicketStatus.CANCELLED);
                                ticketRepository.save(ticket);
                            });
                }
            }

            return eventRepository.save(event);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Event cancellation was interrupted",
                    exception);
        } finally {
            acquiredLocks.forEach(lock -> {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            });
        }
    }
}
