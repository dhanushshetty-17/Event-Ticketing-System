package com.dhanush.ticketing.config;


import com.dhanush.ticketing.event.Event;
import com.dhanush.ticketing.event.EventRepository;
import com.dhanush.ticketing.event.EventSeat;
import com.dhanush.ticketing.event.EventSeatRepository;
import com.dhanush.ticketing.event.EventSeatStatus;
import com.dhanush.ticketing.event.EventStatus;
import com.dhanush.ticketing.user.User;
import com.dhanush.ticketing.user.UserRepository;
import com.dhanush.ticketing.user.UserRole;
import com.dhanush.ticketing.venue.Seat;
import com.dhanush.ticketing.venue.SeatRepository;
import com.dhanush.ticketing.venue.SeatStatus;
import com.dhanush.ticketing.venue.Venue;
import com.dhanush.ticketing.venue.VenueRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.security.SecureRandom;
import java.util.Base64;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${app.seed.demo-data:false}")
    private boolean demoDataEnabled;

    @Value("${DEMO_ORGANIZER_USERNAME:organizer@example.com}")
    private String demoOrganizerUsername;

    @Value("${DEMO_ORGANIZER_PASSWORD:}")
    private String configuredOrganizerPassword;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner initializeData(
            UserRepository userRepository,
            VenueRepository venueRepository,
            SeatRepository seatRepository,
            EventRepository eventRepository,
            EventSeatRepository eventSeatRepository,
            Environment environment) {

        return args -> {
            if (environment.acceptsProfiles(Profiles.of("prod"))
                    && demoDataEnabled) {
                throw new IllegalStateException(
                        "Demo data seeding must be disabled in the prod profile");
            }
            if (!demoDataEnabled) {
                return;
            }

            User user = userRepository.findByEmail("user@example.com")
                    .orElseGet(() -> {
                        User created = new User();
                        created.setEmail("user@example.com");
                        created.setPassword(passwordEncoder().encode(generatePassword()));
                        created.setRole(UserRole.USER);
                        return userRepository.save(created);
                    });

            User organizer = userRepository.findByEmail(demoOrganizerUsername)
                    .orElseGet(() -> {
                        User created = new User();
                        created.setEmail(demoOrganizerUsername);
                        String password = configuredOrganizerPassword.isBlank()
                                ? generatePassword()
                                : configuredOrganizerPassword;
                        created.setPassword(passwordEncoder().encode(password));
                        created.setRole(UserRole.ORGANIZER);
                        return userRepository.save(created);
                    });
            if (!configuredOrganizerPassword.isBlank()) {
                organizer.setPassword(passwordEncoder().encode(configuredOrganizerPassword));
                organizer.setRole(UserRole.ORGANIZER);
                organizer = userRepository.save(organizer);
            }

            Venue conventionCentre = findOrCreateVenue(
                    venueRepository, "Bangalore Convention Centre", "Bangalore, Karnataka");
            Venue amphitheatre = findOrCreateVenue(
                    venueRepository, "The Riverfront Amphitheatre", "Ahmedabad, Gujarat");
            Venue studio = findOrCreateVenue(
                    venueRepository, "Indiranagar Arts Studio", "Bangalore, Karnataka");

            List<Seat> conventionSeats = ensureSeats(seatRepository, conventionCentre, 30);
            List<Seat> amphitheatreSeats = ensureSeats(seatRepository, amphitheatre, 24);
            List<Seat> studioSeats = ensureSeats(seatRepository, studio, 18);

            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            ensureEvent(eventRepository, eventSeatRepository, "Tech Conference 2026",
                    "A high-energy day of product stories, engineering lessons and future-facing ideas.",
                    conventionCentre, organizer, now.plusDays(7).withHour(9).withMinute(30), conventionSeats);
            ensureEvent(eventRepository, eventSeatRepository, "Neon Garden Sessions",
                    "An after-dark electronic music experience with live visuals, local selectors and an open-air dance floor.",
                    amphitheatre, organizer, now.plusDays(14).withHour(19).withMinute(0), amphitheatreSeats);
            ensureEvent(eventRepository, eventSeatRepository, "Designing Tomorrow",
                    "A small-room salon for creators, founders and curious minds shaping what comes next.",
                    studio, organizer, now.plusDays(21).withHour(18).withMinute(30), studioSeats);
            ensureEvent(eventRepository, eventSeatRepository, "The Weekend Food Lab",
                    "Five chefs, one shared table and a tasting menu built around the best produce of the season.",
                    conventionCentre, organizer, now.plusDays(30).withHour(12).withMinute(0), conventionSeats);

            log.info("Demo catalog initialized with multiple venues, events and seats.");
        };
    }

    private Venue findOrCreateVenue(VenueRepository repository, String name, String address) {
        return repository.findAll().stream()
                .filter(item -> item.getName().equals(name))
                .findFirst()
                .orElseGet(() -> repository.save(new Venue(name, address)));
    }

    private List<Seat> ensureSeats(SeatRepository repository, Venue venue, int count) {
        List<Seat> existing = repository.findAll().stream()
                .filter(seat -> seat.getVenue().getId().equals(venue.getId()))
                .toList();
        if (existing.size() >= count) return existing;

        List<Seat> created = new java.util.ArrayList<>(existing);
        String[] rows = {"A", "B", "C", "D", "E"};
        Map<String, String> sections = Map.of("A", "VIP", "B", "VIP", "C", "PREMIUM", "D", "STANDARD", "E", "STANDARD");
        for (String row : rows) {
            for (int number = 1; number <= 6 && created.size() < count; number++) {
                String label = row + number;
                if (created.stream().anyMatch(seat -> seat.getSeatLabel().equals(label))) continue;
                Seat seat = new Seat(venue, label, row, number, SeatStatus.ACTIVE);
                seat.setSection(sections.get(row));
                created.add(repository.save(seat));
            }
        }
        return created;
    }

    private void ensureEvent(
            EventRepository eventRepository,
            EventSeatRepository eventSeatRepository,
            String name,
            String description,
            Venue venue,
            User organizer,
            OffsetDateTime startsAt,
            List<Seat> seats) {
        Optional<Event> existing = eventRepository.findAll().stream()
                .filter(event -> event.getName().equals(name))
                .findFirst();
        Event event = existing.orElseGet(() -> new Event(
                name, description, venue, organizer, startsAt, startsAt.plusHours(4), EventStatus.PUBLISHED));
        event.setDescription(description);
        event.setVenue(venue);
        event.setOrganizer(organizer);
        event.setStartsAt(startsAt);
        event.setEndsAt(startsAt.plusHours(4));
        event.setStatus(EventStatus.PUBLISHED);
        event = eventRepository.save(event);
        List<EventSeat> eventSeats = eventSeatRepository.findAllByEventId(event.getId());
        for (int index = eventSeats.size(); index < seats.size(); index++) {
            EventSeat eventSeat = new EventSeat(event, seats.get(index), EventSeatStatus.AVAILABLE);
            if (index % 11 == 0) eventSeat.setStatus(EventSeatStatus.BOOKED);
            eventSeatRepository.save(eventSeat);
        }
    }

    private String generatePassword() {
        byte[] passwordBytes = new byte[24];
        SECURE_RANDOM.nextBytes(passwordBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(passwordBytes);
    }
}