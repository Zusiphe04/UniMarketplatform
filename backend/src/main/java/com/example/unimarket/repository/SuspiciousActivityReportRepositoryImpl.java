package com.example.unimarket.repository;

import com.example.unimarket.domain.SuspiciousActivityReport;
import com.example.unimarket.domain.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class SuspiciousActivityReportRepositoryImpl implements ISuspiciousActivityReportRepository {
    private final SuspiciousActivityReportJpaRepository jpaRepository;

    public SuspiciousActivityReportRepositoryImpl(SuspiciousActivityReportJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    @Override public SuspiciousActivityReport create(SuspiciousActivityReport value) {
        return value == null ? null : jpaRepository.save(value);
    }
    @Override public SuspiciousActivityReport read(UUID id) {
        return id == null ? null : jpaRepository.findById(id).orElse(null);
    }
    @Override public SuspiciousActivityReport update(SuspiciousActivityReport value) {
        return value == null || value.getId() == null || !jpaRepository.existsById(value.getId())
                ? null : jpaRepository.save(value);
    }
    @Override public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) return false;
        jpaRepository.deleteById(id);
        return true;
    }
    @Override public List<SuspiciousActivityReport> getAll() { return jpaRepository.findAll(); }
    @Override public SuspiciousActivityReport readByIdForUpdate(UUID reportId) {
        return reportId == null ? null : jpaRepository.findByIdForUpdate(reportId).orElse(null);
    }
    @Override public SuspiciousActivityReport readByIdAndReporterId(UUID reportId, UUID reporterId) {
        return reportId == null || reporterId == null ? null
                : jpaRepository.findByIdAndReporterId(reportId, reporterId).orElse(null);
    }
    @Override public Page<SuspiciousActivityReport> readByReporterId(UUID reporterId, Pageable pageable) {
        return jpaRepository.findByReporterIdOrderByCreatedAtDesc(reporterId, pageable);
    }
    @Override public Page<SuspiciousActivityReport> readByStatus(ReportStatus status, Pageable pageable) {
        return jpaRepository.findByStatusOrderByCreatedAtAsc(status, pageable);
    }
}
