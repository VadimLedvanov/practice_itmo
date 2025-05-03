package ru.ledvanov.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ledvanov.entity.Event;
import ru.ledvanov.entity.enums.EventCategory;


import java.util.UUID;

public interface EventDAO extends JpaRepository<Event, UUID> {
    Page<Event> findEventsByCategory(EventCategory category, Pageable pageable);

    @Query(
            "SELECT e FROM Event e " +
            "WHERE e.category = :category " +
            "AND e.id NOT IN (" +
            "  SELECT v.event.id FROM View v WHERE v.appUser.id = :userId" +
            ")"
           )
    Page<Event> findNotViewedEventsByCategory(@Param("category") EventCategory category,
                                              @Param("userId") UUID userId,
                                              Pageable pageable);

}
