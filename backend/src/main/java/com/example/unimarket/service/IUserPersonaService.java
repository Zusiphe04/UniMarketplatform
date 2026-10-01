package com.example.unimarket.service;

import com.example.unimarket.domain.enums.CommunityPersona;
import com.example.unimarket.request.DeclarePersonaRequest;
import com.example.unimarket.response.PersonaResponse;

import java.util.List;
import java.util.UUID;

/** Community persona declaration and verification use cases. */
public interface IUserPersonaService {
    List<PersonaResponse> getPersonas(UUID userId);
    PersonaResponse declare(UUID userId, DeclarePersonaRequest request);
    PersonaResponse verify(UUID userId, CommunityPersona persona, UUID actingAdminId);
}
