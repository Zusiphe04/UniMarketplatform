package com.example.unimarket.repository;

import com.example.unimarket.domain.SuspiciousActivityReport;
import com.example.unimarket.domain.enums.ReportStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SuspiciousActivityReportJpaRepository
        extends JpaRepository<SuspiciousActivityReport, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from SuspiciousActivityReport r where r.id = :reportId")
    Optional<SuspiciousActivityReport> findByIdForUpdate(@Param("reportId") UUID reportId);
    Optional<SuspiciousActivityReport> findByIdAndReporterId(UUID reportId, UUID reporterId);
    Page<SuspiciousActivityReport> findByReporterIdOrderByCreatedAtDesc(UUID reporterId, Pageable pageable);
    Page<SuspiciousActivityReport> findByStatusOrderByCreatedAtAsc(ReportStatus status, Pageable pageable);
}
