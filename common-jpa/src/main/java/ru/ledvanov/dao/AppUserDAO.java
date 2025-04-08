package ru.ledvanov.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ledvanov.entity.AppUser;

import java.util.Optional;
import java.util.UUID;

public interface AppUserDAO extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByTelegramUserId(Long id);
    Optional<AppUser> findById(UUID id);
}
