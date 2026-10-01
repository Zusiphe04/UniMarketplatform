package com.example.unimarket.service;

import com.example.unimarket.domain.enums.ReportStatus;
import com.example.unimarket.request.CreateReportRequest;
import com.example.unimarket.request.ResolveReportRequest;
import com.example.unimarket.response.ModerationReportResponse;
import com.example.unimarket.response.ReportResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ISuspiciousActivityReportService {
    ReportResponse create(UUID reporterId, CreateReportRequest request);
    Page<ReportResponse> listMine(UUID reporterId, int page, int size);
    ReportResponse getMine(UUID reporterId, UUID reportId);
    Page<ModerationReportResponse> listQueue(ReportStatus status, int page, int size);
    ModerationReportResponse getForModeration(UUID reportId);
    ModerationReportResponse resolve(UUID moderatorId, UUID reportId, ResolveReportRequest request);
}
