package ru.ledvanov.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.ledvanov.entity.Event;
import ru.ledvanov.entity.enums.EventCategory;


import java.util.UUID;

public interface EventDAO extends JpaRepository<Event, UUID> {
    Page<Event> findEventsByCategory(EventCategory category, Pageable pageable);
}
