package com.example.unimarket.repository;

import com.example.unimarket.domain.UserPersonaAssignment;
import com.example.unimarket.domain.enums.CommunityPersona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Spring Data access to community persona assignments. */
public interface UserPersonaAssignmentJpaRepository extends JpaRepository<UserPersonaAssignment, UUID> {
    List<UserPersonaAssignment> findByUserIdOrderByPersona(UUID userId);
    Optional<UserPersonaAssignment> findByUserIdAndPersona(UUID userId, CommunityPersona persona);
}
