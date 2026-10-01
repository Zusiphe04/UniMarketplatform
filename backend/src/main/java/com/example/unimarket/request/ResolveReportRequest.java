package com.example.unimarket.request;

import com.example.unimarket.domain.enums.ModerationAction;
import com.example.unimarket.domain.enums.ReportResolution;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolveReportRequest(
        @NotNull ReportResolution resolution,
        @NotNull ModerationAction action,
        @NotBlank(message = "A moderation note is required") @Size(max = 1000) String note,
        @Size(max = 500) String reporterMessage
) { }
