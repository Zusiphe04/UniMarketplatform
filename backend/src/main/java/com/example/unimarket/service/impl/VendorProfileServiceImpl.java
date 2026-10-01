package com.example.unimarket.service.impl;

import com.example.unimarket.domain.UserAccount;
import com.example.unimarket.domain.UserPersonaAssignment;
import com.example.unimarket.domain.VendorProfile;
import com.example.unimarket.domain.enums.AccountStatus;
import com.example.unimarket.domain.enums.CommunityPersona;
import com.example.unimarket.domain.enums.NotificationType;
import com.example.unimarket.domain.enums.Role;
import com.example.unimarket.domain.enums.VendorVerificationStatus;
import com.example.unimarket.exception.ResourceNotFoundException;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.UserPersonaAssignmentFactory;
import com.example.unimarket.factory.VendorProfileFactory;
import com.example.unimarket.repository.IUserAccountRepository;
import com.example.unimarket.repository.IUserPersonaAssignmentRepository;
import com.example.unimarket.repository.IVendorProfileRepository;
import com.example.unimarket.request.ReviewVendorProfileRequest;
import com.example.unimarket.request.SubmitVendorProfileRequest;
import com.example.unimarket.response.VendorProfileResponse;
import com.example.unimarket.service.INotificationService;
import com.example.unimarket.service.IRoleService;
import com.example.unimarket.service.IVendorProfileService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class VendorProfileServiceImpl implements IVendorProfileService {
    private final IVendorProfileRepository vendorRepository;
    private final IUserAccountRepository accountRepository;
    private final IUserPersonaAssignmentRepository personaRepository;
    private final IRoleService roleService;
    private final INotificationService notificationService;

    public VendorProfileServiceImpl(IVendorProfileRepository vendorRepository,
                                    IUserAccountRepository accountRepository,
                                    IUserPersonaAssignmentRepository personaRepository,
                                    IRoleService roleService,
                                    INotificationService notificationService) {
        this.vendorRepository = vendorRepository;
        this.accountRepository = accountRepository;
        this.personaRepository = personaRepository;
        this.roleService = roleService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public VendorProfileResponse getOwn(UUID userId) {
        VendorProfile profile = vendorRepository.readByUserId(userId);
        if (profile == null) throw ResourceNotFoundException.of("Vendor profile");
        return VendorProfileResponse.from(profile);
    }

    @Override
    @Transactional
    public VendorProfileResponse submit(UUID userId, SubmitVendorProfileRequest request) {
        UserAccount account = accountRepository.read(userId);
        if (account == null) throw ResourceNotFoundException.of("Account");
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ValidationException("Only an active account can submit a vendor profile.");
        }

        VendorProfile existing = vendorRepository.readByUserId(userId);
        if (roleService.getActiveRoles(userId).contains(Role.SELLER)) {
            throw new ValidationException("This account is already an approved vendor.");
        }
        if (existing != null) {
            switch (existing.getVerificationStatus()) {
                case VERIFIED -> throw new ValidationException("This vendor application is already approved.");
                case PENDING -> throw new ValidationException("A vendor application is already awaiting review.");
                case SUSPENDED -> throw new ValidationException("A suspended vendor profile cannot be registered again.");
                case REJECTED -> { /* Rejected applications may be corrected and resubmitted. */ }
            }
        }

        VendorProfile value = existing == null
                ? VendorProfileFactory.create(userId, request.vendorType(), request.businessName(),
                        request.description(), request.registrationNumber(), request.websiteUrl())
                : VendorProfileFactory.resubmit(existing, request.vendorType(), request.businessName(),
                        request.description(), request.registrationNumber(), request.websiteUrl());
        if (value == null) {
            throw new ValidationException("The vendor profile details are invalid.");
        }
        return VendorProfileResponse.from(existing == null
                ? vendorRepository.create(value) : vendorRepository.update(value));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorProfileResponse> listForReview(VendorVerificationStatus status) {
        return vendorRepository.readByStatus(status).stream().map(VendorProfileResponse::from).toList();
    }

    @Override
    @Transactional
    public VendorProfileResponse review(UUID userId, ReviewVendorProfileRequest request, UUID adminId) {
        if (request.status() == VendorVerificationStatus.PENDING) {
            throw new ValidationException("A review must approve, reject, or suspend the vendor.");
        }
        VendorProfile profile = vendorRepository.readByUserId(userId);
        if (profile == null) throw ResourceNotFoundException.of("Vendor profile");
        VendorProfile reviewed = VendorProfileFactory.review(
                profile, request.status(), adminId, request.reviewNote());
        if (reviewed == null) throw new ValidationException("The vendor review is invalid.");
        VendorProfile saved = vendorRepository.update(reviewed);

        if (request.status() == VendorVerificationStatus.VERIFIED) {
            verifyVendorPersona(saved, adminId);
            roleService.grant(userId, Role.SELLER, adminId);
        } else {
            if (roleService.getActiveRoles(userId).contains(Role.SELLER)) {
                roleService.revoke(userId, Role.SELLER, adminId);
            }
        }
        sendReviewNotification(saved);
        return VendorProfileResponse.from(saved);
    }

    private void verifyVendorPersona(VendorProfile profile, UUID adminId) {
        UserPersonaAssignment assignment = personaRepository.readByUserIdAndPersona(
                profile.getUserId(), CommunityPersona.VENDOR);
        if (assignment == null) {
            assignment = UserPersonaAssignmentFactory.declare(
                    profile.getUserId(), CommunityPersona.VENDOR, profile.getBusinessName(), null);
            assignment = personaRepository.create(assignment);
        }
        if (!assignment.isVerified()) {
            UserPersonaAssignmentFactory.verify(assignment, adminId);
            personaRepository.update(assignment);
        }
    }

    private void sendReviewNotification(VendorProfile profile) {
        NotificationType type = switch (profile.getVerificationStatus()) {
            case VERIFIED -> NotificationType.VENDOR_APPROVED;
            case REJECTED -> NotificationType.VENDOR_REJECTED;
            case SUSPENDED -> NotificationType.VENDOR_SUSPENDED;
            case PENDING -> throw new IllegalStateException("A pending vendor profile cannot be reviewed");
        };
        String statusText = profile.getVerificationStatus().name().toLowerCase();
        notificationService.send(
                profile.getUserId(),
                type,
                "Vendor profile " + statusText,
                "Your vendor profile for " + profile.getBusinessName() + " was " + statusText + ".",
                "VENDOR_PROFILE",
                profile.getId(),
                "vendor-review:" + profile.getId() + ":" + profile.getReviewedAt());
    }
}
