package com.dhanush.ticketing.event;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    @Override
    @EntityGraph(attributePaths = {"venue", "organizer"})
    List<Event> findAll();

    @Override
    @EntityGraph(attributePaths = {"venue", "organizer"})
    Optional<Event> findById(UUID id);
}