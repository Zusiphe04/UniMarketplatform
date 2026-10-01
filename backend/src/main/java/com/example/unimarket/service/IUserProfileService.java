package com.example.unimarket.service;

import com.example.unimarket.request.UpdateProfileRequest;
import com.example.unimarket.response.UserProfileResponse;

import java.util.UUID;

/**
 * Manages a member's own profile.
 */
public interface IUserProfileService {

    /**
     * Reads a profile by owning account.
     *
     * @throws com.example.unimarket.exception.ResourceNotFoundException when absent
     */
    UserProfileResponse getByUserId(UUID userId);

    /**
     * Updates the caller's profile.
     *
     * <p>The account identifier comes from the authenticated principal, so one
     * member cannot edit another member's profile by supplying a different id.
     */
    UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);
}
