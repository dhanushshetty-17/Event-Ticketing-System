package com.dhanush.ticketing.event;

import com.dhanush.ticketing.exception.ResourceNotFoundException;
import com.dhanush.ticketing.user.User;
import com.dhanush.ticketing.user.UserRepository;
import com.dhanush.ticketing.venue.Seat;
import com.dhanush.ticketing.venue.SeatRepository;
import com.dhanush.ticketing.venue.SeatStatus;
import com.dhanush.ticketing.venue.Venue;
import com.dhanush.ticketing.venue.VenueRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final EventSeatRepository eventSeatRepository;

    public EventController(
            EventRepository eventRepository,
            VenueRepository venueRepository,
            UserRepository userRepository,
            SeatRepository seatRepository,
            EventSeatRepository eventSeatRepository) {

        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.userRepository = userRepository;
        this.seatRepository = seatRepository;
        this.eventSeatRepository = eventSeatRepository;
    }

    @GetMapping("/{id}/seats")
    public List<EventSeatResponse> getEventSeats(@PathVariable UUID id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found");
        }

        return eventSeatRepository.findAllByEventIdWithSeat(id)
                .stream()
                .map(eventSeat -> new EventSeatResponse(
                        eventSeat.getSeat().getId(),
                        eventSeat.getSeat().getSeatLabel(),
                        eventSeat.getStatus()
                ))
                .toList();
    }

    @GetMapping
    public List<EventResponse> getAllEvents() {

        return eventRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public EventResponse getEvent(@PathVariable UUID id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Event not found"));

        return toResponse(event);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(
            @Valid @RequestBody EventCreateRequest request,
            Authentication authentication) {

        UUID organizerId = UUID.fromString(authentication.getName());

        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Organizer not found"));

        if (organizer.getRole() != com.dhanush.ticketing.user.UserRole.ORGANIZER) {
            throw new IllegalStateException(
                    "Only organizers can create events");
        }

        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Venue not found"));

        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new IllegalArgumentException(
                    "Event end time must be after start time");
        }

        Event event = new Event();

        event.setName(request.name());
        event.setDescription(request.description());
        event.setVenue(venue);
        event.setOrganizer(organizer);
        event.setStartsAt(request.startsAt());
        event.setEndsAt(request.endsAt());
        event.setStatus(EventStatus.DRAFT);

        Event savedEvent = eventRepository.save(event);

        List<Seat> activeSeats = seatRepository.findAll()
                .stream()
                .filter(seat ->
                        seat.getVenue().getId().equals(venue.getId())
                                && seat.getStatus() == SeatStatus.ACTIVE)
                .toList();

        List<EventSeat> eventSeats = activeSeats
                .stream()
                .map(seat -> new EventSeat(
                        savedEvent,
                        seat,
                        EventSeatStatus.AVAILABLE))
                .toList();

        eventSeatRepository.saveAll(eventSeats);

        Event responseEvent = eventRepository.findById(savedEvent.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Event not found"));

        return toResponse(responseEvent);
    }

    @PatchMapping("/{id}/publish")
    public EventResponse publishEvent(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID organizerId = UUID.fromString(authentication.getName());

        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Organizer not found"));

        if (organizer.getRole() != com.dhanush.ticketing.user.UserRole.ORGANIZER) {
            throw new IllegalStateException(
                    "Only organizers can publish events");
        }

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Event not found"));

        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new IllegalStateException(
                    "You are not allowed to publish this event");
        }

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT events can be published");
        }

        if (!event.getStartsAt().isAfter(
                java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC))) {

            throw new IllegalStateException(
                    "Event start time must be in the future");
        }

        boolean hasAvailableSeats = eventSeatRepository.findAll()
                .stream()
                .anyMatch(eventSeat ->
                        eventSeat.getEvent().getId().equals(event.getId())
                                && eventSeat.getStatus()
                                == EventSeatStatus.AVAILABLE);

        if (!hasAvailableSeats) {
            throw new IllegalStateException(
                    "Event must have at least one available seat");
        }

        event.setStatus(EventStatus.PUBLISHED);

        Event savedEvent = eventRepository.save(event);

        Event responseEvent = eventRepository.findById(savedEvent.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Event not found"));

        return toResponse(responseEvent);
    }

    private EventResponse toResponse(Event event) {

        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getVenue().getId(),
                event.getVenue().getName(),
                event.getOrganizer().getId(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getStatus()
        );
    }
}