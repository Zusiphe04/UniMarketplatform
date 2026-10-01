package com.example.unimarket.repository;

import com.example.unimarket.domain.UserPersonaAssignment;
import com.example.unimarket.domain.enums.CommunityPersona;

import java.util.List;
import java.util.UUID;

/** Persistence contract for community persona declarations. */
public interface IUserPersonaAssignmentRepository extends IRepository<UserPersonaAssignment, UUID> {
    List<UserPersonaAssignment> readByUserId(UUID userId);
    UserPersonaAssignment readByUserIdAndPersona(UUID userId, CommunityPersona persona);
}
