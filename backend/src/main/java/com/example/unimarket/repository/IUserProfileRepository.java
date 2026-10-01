package com.example.unimarket.repository;

import com.example.unimarket.domain.UserProfile;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Abstraction over profile persistence. */
public interface IUserProfileRepository extends IRepository<UserProfile, UUID> {
    UserProfile readByUserId(UUID userId);
    List<UserProfile> readByUserIds(Collection<UUID> userIds);
}
