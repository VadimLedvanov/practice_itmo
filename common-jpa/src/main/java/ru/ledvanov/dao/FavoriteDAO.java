package ru.ledvanov.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.ledvanov.entity.AppUser;
import ru.ledvanov.entity.Favorite;

import java.util.UUID;

public interface FavoriteDAO extends JpaRepository<Favorite, UUID> {
    Page<Favorite> findByAppUser(AppUser user, Pageable pageable);
}
