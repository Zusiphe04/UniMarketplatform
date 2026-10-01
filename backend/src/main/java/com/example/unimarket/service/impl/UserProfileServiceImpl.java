package com.example.unimarket.service.impl;

import com.example.unimarket.domain.UserProfile;
import com.example.unimarket.exception.ResourceNotFoundException;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.UserProfileFactory;
import com.example.unimarket.repository.IUserProfileRepository;
import com.example.unimarket.request.UpdateProfileRequest;
import com.example.unimarket.response.UserProfileResponse;
import com.example.unimarket.service.IUserProfileService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Profile reads and updates for the authenticated member.
 */
@Service
public class UserProfileServiceImpl implements IUserProfileService {

    private final IUserProfileRepository profileRepository;

    public UserProfileServiceImpl(IUserProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getByUserId(UUID userId) {
        UserProfile profile = profileRepository.readByUserId(userId);
        if (profile == null) {
            throw ResourceNotFoundException.of("Profile");
        }
        return UserProfileResponse.from(profile);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        UserProfile existing = profileRepository.readByUserId(userId);
        if (existing == null) {
            throw ResourceNotFoundException.of("Profile");
        }

        UserProfile updated = UserProfileFactory.updateDetails(
                existing,
                request.displayName(),
                request.phoneNumber(),
                request.bio());

        // The factory returns null when a supplied phone number is malformed,
        // rather than silently discarding it.
        if (updated == null) {
            throw new ValidationException("The profile details supplied are not valid.");
        }

        return UserProfileResponse.from(profileRepository.update(updated));
    }
}
