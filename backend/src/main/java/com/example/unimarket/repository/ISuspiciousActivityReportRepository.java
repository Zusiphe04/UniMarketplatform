package com.example.unimarket.repository;

import com.example.unimarket.domain.SuspiciousActivityReport;
import com.example.unimarket.domain.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ISuspiciousActivityReportRepository extends IRepository<SuspiciousActivityReport, UUID> {
    SuspiciousActivityReport readByIdForUpdate(UUID reportId);
    SuspiciousActivityReport readByIdAndReporterId(UUID reportId, UUID reporterId);
    Page<SuspiciousActivityReport> readByReporterId(UUID reporterId, Pageable pageable);
    Page<SuspiciousActivityReport> readByStatus(ReportStatus status, Pageable pageable);
}
