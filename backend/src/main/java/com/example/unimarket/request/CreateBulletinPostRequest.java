package com.example.unimarket.request;

import com.example.unimarket.domain.enums.BulletinPostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateBulletinPostRequest(
        @NotNull(message = "Post type is required") BulletinPostType type,
        @NotBlank(message = "Title is required") @Size(max = 160) String title,
        @NotBlank(message = "Content is required") @Size(max = 5000) String content,
        @Size(max = 200) String location,
        @Size(max = 1000) String coverImageUrl,
        Instant eventStartsAt,
        @Positive(message = "Event capacity must be positive") Integer eventCapacity,
        Instant expiresAt,
        boolean publishNow
) { }
