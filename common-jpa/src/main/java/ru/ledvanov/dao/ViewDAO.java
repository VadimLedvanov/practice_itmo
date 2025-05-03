package ru.ledvanov.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ledvanov.entity.View;

import javax.transaction.Transactional;
import java.util.UUID;

public interface ViewDAO extends JpaRepository<View, UUID> {
    @Transactional
    @Modifying
    @Query("DELETE FROM View v WHERE v.event.id = :eventId")
    void deleteByEventId(@Param("eventId") UUID eventId);
}
