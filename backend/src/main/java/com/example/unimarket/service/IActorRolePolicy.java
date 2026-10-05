package com.example.unimarket.service;

import java.util.UUID;

/** Authoritative access checks based on the actor's current persisted roles. */
public interface IActorRolePolicy {
    void requireBuyerOnly(UUID actorId);
    void requireSellerOnly(UUID actorId);
}
