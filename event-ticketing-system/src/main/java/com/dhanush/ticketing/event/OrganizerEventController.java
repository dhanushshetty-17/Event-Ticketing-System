package com.dhanush.ticketing.event;

import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizer/events")
public class OrganizerEventController {

    private final EventRepository eventRepository;
    private final EventSeatRepository eventSeatRepository;

    public OrganizerEventController(
            EventRepository eventRepository,
            EventSeatRepository eventSeatRepository) {
        this.eventRepository = eventRepository;
        this.eventSeatRepository = eventSeatRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<OrganizerEventResponse> getOrganizerEvents(
            Authentication authentication) {
        UUID organizerId = UUID.fromString(authentication.getName());
        return eventRepository.findAllByOrganizer_Id(organizerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/summary")
    @Transactional(readOnly = true)
    public List<OrganizerEventSummaryResponse> getOrganizerEventSummaries(
            Authentication authentication) {
        UUID organizerId = UUID.fromString(authentication.getName());
        return eventRepository.findAllByOrganizer_Id(organizerId).stream()
                .map(this::toSummary)
                .toList();
    }

    private OrganizerEventResponse toResponse(Event event) {
        List<EventSeat> seats = eventSeatRepository.findAllByEventId(event.getId());
        long reservedSeats = seats.stream()
                .filter(seat -> seat.getStatus() == EventSeatStatus.RESERVED)
                .count();
        long soldSeats = seats.stream()
                .filter(seat -> seat.getStatus() == EventSeatStatus.BOOKED)
                .count();

        return new OrganizerEventResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getVenue().getId(),
                event.getVenue().getName(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getStatus(),
                seats.size(),
                reservedSeats,
                soldSeats,
                null
        );
    }

    private OrganizerEventSummaryResponse toSummary(Event event) {
        List<EventSeat> seats = eventSeatRepository.findAllByEventId(event.getId());
        var sections = seats.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        seat -> seat.getSeat().getSection() == null
                                || seat.getSeat().getSection().isBlank()
                                ? "Unassigned"
                                : seat.getSeat().getSection()))
                .entrySet()
                .stream()
                .map(entry -> new SectionOccupancy(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream()
                                .filter(seat -> seat.getStatus() == EventSeatStatus.RESERVED)
                                .count(),
                        entry.getValue().stream()
                                .filter(seat -> seat.getStatus() == EventSeatStatus.BOOKED)
                                .count()))
                .toList();

        return new OrganizerEventSummaryResponse(
                event.getId(),
                seats.size(),
                seats.stream().filter(seat -> seat.getStatus() == EventSeatStatus.RESERVED).count(),
                seats.stream().filter(seat -> seat.getStatus() == EventSeatStatus.BOOKED).count(),
                null,
                sections);
    }
}
