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

            if (userRepository.findByEmail("user@example.com").isPresent()) {
                return;
            }

            User user = new User();
            user.setEmail("user@example.com");
            user.setPassword(passwordEncoder().encode(generatePassword()));
            user.setRole(UserRole.USER);
            user = userRepository.save(user);

            User organizer = new User();
            organizer.setEmail(demoOrganizerUsername);
            String organizerPassword = configuredOrganizerPassword.isBlank()
                    ? generatePassword()
                    : configuredOrganizerPassword;
            organizer.setPassword(passwordEncoder().encode(organizerPassword));
            organizer.setRole(UserRole.ORGANIZER);
            organizer = userRepository.save(organizer);

            Venue venue = new Venue();
            venue.setName("Bangalore Convention Centre");
            venue.setAddress("Bangalore, Karnataka");
            venue = venueRepository.save(venue);

            Seat seat1 = new Seat();
            seat1.setVenue(venue);
            seat1.setSeatLabel("A1");
            seat1.setRowName("A");
            seat1.setSeatNumber(1);
            seat1.setStatus(SeatStatus.ACTIVE);
            seat1 = seatRepository.save(seat1);

            Seat seat2 = new Seat();
            seat2.setVenue(venue);
            seat2.setSeatLabel("A2");
            seat2.setRowName("A");
            seat2.setSeatNumber(2);
            seat2.setStatus(SeatStatus.ACTIVE);
            seat2 = seatRepository.save(seat2);

            Event event = new Event();
            event.setName("Tech Conference 2026");
            event.setDescription("Technology and software engineering conference.");
            event.setVenue(venue);
            event.setOrganizer(organizer);
            event.setStartsAt(
                    OffsetDateTime.now(ZoneOffset.UTC).plusDays(7)
            );
            event.setEndsAt(
                    OffsetDateTime.now(ZoneOffset.UTC).plusDays(7).plusHours(4)
            );
            event.setStatus(EventStatus.PUBLISHED);
            event = eventRepository.save(event);

            EventSeat eventSeat1 = new EventSeat();
            eventSeat1.setEvent(event);
            eventSeat1.setSeat(seat1);
            eventSeat1.setStatus(EventSeatStatus.AVAILABLE);
            eventSeatRepository.save(eventSeat1);

            EventSeat eventSeat2 = new EventSeat();
            eventSeat2.setEvent(event);
            eventSeat2.setSeat(seat2);
            eventSeat2.setStatus(EventSeatStatus.AVAILABLE);
            eventSeatRepository.save(eventSeat2);

            log.info("Demo data initialized. Generated credentials are not logged.");
        };
    }

    private String generatePassword() {
        byte[] passwordBytes = new byte[24];
        SECURE_RANDOM.nextBytes(passwordBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(passwordBytes);
    }
}