package com.dhanush.ticketing.venue;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.dhanush.ticketing.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueRepository venueRepository;

    public VenueController(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @GetMapping
    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }

    @GetMapping("/{id}")
    public Venue getVenue(@PathVariable UUID id) {

        return venueRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Venue not found: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Venue createVenue(
            @Valid @RequestBody VenueCreateRequest request) {

        Venue venue = new Venue();

        venue.setName(request.name());
        venue.setAddress(request.address());

        return venueRepository.save(venue);
    }
}