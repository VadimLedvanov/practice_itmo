package ru.ledvanov.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ledvanov.entity.AppUser;
import ru.ledvanov.entity.Favorite;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteDAO extends JpaRepository<Favorite, UUID> {
    Page<Favorite> findByAppUser(AppUser user, Pageable pageable);
    Optional<Favorite> findByEventId(UUID eventId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.event.id = :eventId")
    void deleteByEventId(@Param("eventId") UUID eventId);
}
