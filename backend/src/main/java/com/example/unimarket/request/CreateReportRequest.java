package com.example.unimarket.request;

import com.example.unimarket.domain.enums.ReportReason;
import com.example.unimarket.domain.enums.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateReportRequest(
        @NotNull ReportTargetType targetType,
        @NotNull UUID targetId,
        @NotNull ReportReason reason,
        @NotBlank(message = "Report details are required") @Size(max = 2000) String details
) { }
