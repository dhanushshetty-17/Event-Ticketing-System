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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Configuration
public class DataInitializer {

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
            EventSeatRepository eventSeatRepository) {

        return args -> {

            if (userRepository.findByEmail("user@example.com").isPresent()) {
                return;
            }

            User user = new User();
            user.setEmail("user@example.com");
            user.setPassword(passwordEncoder().encode("password123"));
            user.setRole(UserRole.USER);
            user = userRepository.save(user);

            User organizer = new User();
            organizer.setEmail("organizer@example.com");
            organizer.setPassword(passwordEncoder().encode("organizer123"));
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

            System.out.println("Test data initialized successfully.");
            System.out.println("User: user@example.com / password123");
            System.out.println("Organizer: organizer@example.com / organizer123");
        };
    }
}