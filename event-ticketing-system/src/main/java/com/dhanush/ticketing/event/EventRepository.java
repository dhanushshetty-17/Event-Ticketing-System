package com.dhanush.ticketing.event;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    @EntityGraph(attributePaths = {"venue", "organizer"})
    Page<Event> findAllByStatusNot(EventStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"venue", "organizer"})
    Optional<Event> findById(UUID id);

    @EntityGraph(attributePaths = {"venue", "organizer"})
    List<Event> findAllByOrganizer_Id(UUID organizerId);
}