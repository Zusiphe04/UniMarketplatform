package com.example.unimarket.service.impl;

import com.example.unimarket.domain.UserAccount;
import com.example.unimarket.domain.UserPersonaAssignment;
import com.example.unimarket.domain.enums.AccountStatus;
import com.example.unimarket.domain.enums.CommunityPersona;
import com.example.unimarket.exception.ResourceNotFoundException;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.UserPersonaAssignmentFactory;
import com.example.unimarket.repository.IUserAccountRepository;
import com.example.unimarket.repository.IUserPersonaAssignmentRepository;
import com.example.unimarket.request.DeclarePersonaRequest;
import com.example.unimarket.response.InstitutionEmailValidationResponse;
import com.example.unimarket.response.PersonaResponse;
import com.example.unimarket.service.IInstitutionEmailPolicyService;
import com.example.unimarket.service.IUserPersonaService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserPersonaServiceImpl implements IUserPersonaService {
    private final IUserPersonaAssignmentRepository personaRepository;
    private final IUserAccountRepository accountRepository;
    private final IInstitutionEmailPolicyService institutionEmailPolicyService;

    public UserPersonaServiceImpl(
            IUserPersonaAssignmentRepository personaRepository,
            IUserAccountRepository accountRepository,
            IInstitutionEmailPolicyService institutionEmailPolicyService) {
        this.personaRepository = personaRepository;
        this.accountRepository = accountRepository;
        this.institutionEmailPolicyService = institutionEmailPolicyService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaResponse> getPersonas(UUID userId) {
        ensureAccount(userId);
        return personaRepository.readByUserId(userId).stream().map(PersonaResponse::from).toList();
    }

    @Override
    @Transactional
    public PersonaResponse declare(UUID userId, DeclarePersonaRequest request) {
        UserAccount account = ensureAccount(userId);
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ValidationException("Only an active account can declare a persona.");
        }
        if (request.persona() == CommunityPersona.VENDOR) {
            throw new ValidationException("Submit a vendor profile for administrator verification.");
        }

        boolean academicPersona = request.persona() == CommunityPersona.STUDENT
                || request.persona() == CommunityPersona.FACULTY;
        if (!academicPersona && (request.qualification() != null || request.yearOfStudy() != null)) {
            throw new ValidationException("Academic details are only available for student and faculty personas.");
        }
        if (request.persona() != CommunityPersona.STUDENT && request.yearOfStudy() != null) {
            throw new ValidationException("Year of study is only available for student personas.");
        }

        UserPersonaAssignment existing = personaRepository.readByUserIdAndPersona(userId, request.persona());
        if (existing != null) return PersonaResponse.from(existing);

        String organizationName = request.organizationName();
        String verifiedDomain = null;
        if (academicPersona) {
            InstitutionEmailValidationResponse policyMatch = institutionEmailPolicyService.validate(
                    request.organizationName(), request.persona(), account.getEmail());
            organizationName = policyMatch.institutionName();
            verifiedDomain = policyMatch.matchedDomain();
        }

        UserPersonaAssignment assignment = UserPersonaAssignmentFactory.declare(
                userId, request.persona(), organizationName, request.qualification(),
                request.yearOfStudy(), verifiedDomain);
        if (assignment == null) throw new ValidationException("The persona declaration is invalid.");
        return PersonaResponse.from(personaRepository.create(assignment));
    }

    @Override
    @Transactional
    public PersonaResponse verify(UUID userId, CommunityPersona persona, UUID actingAdminId) {
        ensureAccount(userId);
        if (persona == CommunityPersona.VENDOR) {
            throw new ValidationException("Vendor verification must use the vendor review endpoint.");
        }
        UserPersonaAssignment assignment = personaRepository.readByUserIdAndPersona(userId, persona);
        if (assignment == null) throw ResourceNotFoundException.of("Persona assignment");
        if (assignment.isVerified()) return PersonaResponse.from(assignment);
        UserPersonaAssignment verified = UserPersonaAssignmentFactory.verify(assignment, actingAdminId);
        if (verified == null) throw new ValidationException("The persona could not be verified.");
        return PersonaResponse.from(personaRepository.update(verified));
    }

    private UserAccount ensureAccount(UUID userId) {
        UserAccount account = accountRepository.read(userId);
        if (account == null) throw ResourceNotFoundException.of("Account");
        return account;
    }
}
