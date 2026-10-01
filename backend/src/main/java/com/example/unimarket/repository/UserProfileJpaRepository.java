package com.example.unimarket.repository;

import com.example.unimarket.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Spring Data JPA access to the user_profile table. */
public interface UserProfileJpaRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByUserId(UUID userId);
    List<UserProfile> findByUserIdIn(Collection<UUID> userIds);
}
