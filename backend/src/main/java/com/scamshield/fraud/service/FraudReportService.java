package com.scamshield.fraud.service;

import com.scamshield.entity.User;
import com.scamshield.exception.ResourceNotFoundException;
import com.scamshield.exception.UnauthorizedException;
import com.scamshield.fraud.dto.FraudReportRequest;
import com.scamshield.fraud.dto.FraudReportResponse;
import com.scamshield.fraud.entity.FraudAuditEvent;
import com.scamshield.fraud.entity.FraudCategory;
import com.scamshield.fraud.entity.FraudReport;
import com.scamshield.fraud.entity.ReportedEntity;
import com.scamshield.fraud.mapper.FraudMapper;
import com.scamshield.fraud.repository.FraudAuditEventRepository;
import com.scamshield.fraud.repository.FraudCategoryRepository;
import com.scamshield.fraud.repository.FraudReportRepository;
import com.scamshield.fraud.repository.ReportedEntityRepository;
import com.scamshield.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudReportService {

    private final FraudReportRepository fraudReportRepository;
    private final ReportedEntityRepository reportedEntityRepository;
    private final FraudCategoryRepository fraudCategoryRepository;
    private final UserRepository userRepository;
    private final FraudAuditEventRepository auditEventRepository;
    private final FraudMapper fraudMapper;

    @Transactional
    public FraudReportResponse createReport(FraudReportRequest request) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User reporter = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthorizedException("Reporter details not found in session"));

        FraudCategory category = fraudCategoryRepository.findByCode(request.getCategoryCode())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid fraud category code: " + request.getCategoryCode()));

        ReportedEntity entity = reportedEntityRepository.findByTypeAndValue(request.getEntityType(), request.getEntityValue())
                .orElseGet(() -> {
                    ReportedEntity newEntity = ReportedEntity.builder()
                            .type(request.getEntityType())
                            .value(request.getEntityValue())
                            .isActive(true)
                            .build();
                    return reportedEntityRepository.save(newEntity);
                });

        FraudReport report = FraudReport.builder()
                .entity(entity)
                .reporter(reporter)
                .category(category)
                .description(request.getDescription())
                .severity(request.getSeverity())
                .status("PENDING")
                .build();

        FraudReport saved = fraudReportRepository.save(report);

        auditEventRepository.save(FraudAuditEvent.builder()
                .eventType("REPORT_CREATED")
                .entityId(saved.getId().toString())
                .details("Fraud report submitted for entity: " + entity.getValue() + " of category " + category.getCode())
                .build());

        return fraudMapper.toReportResponse(saved);
    }

    @Transactional(readOnly = true)
    public FraudReportResponse getReportById(Long id) {
        FraudReport report = fraudReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fraud report not found"));
        return fraudMapper.toReportResponse(report);
    }

    @Transactional(readOnly = true)
    public Page<FraudReportResponse> searchReports(String value, Pageable pageable) {
        Page<FraudReport> page;
        if (value == null || value.trim().isEmpty()) {
            page = fraudReportRepository.findAll(pageable);
        } else {
            page = fraudReportRepository.findByEntityValueContainingIgnoreCase(value, pageable);
        }
        return page.map(fraudMapper::toReportResponse);
    }

    @Transactional
    public FraudReportResponse approveReport(Long id) {
        return updateReportStatus(id, "APPROVED");
    }

    @Transactional
    public FraudReportResponse rejectReport(Long id) {
        return updateReportStatus(id, "REJECTED");
    }

    @Transactional
    public FraudReportResponse flagReport(Long id) {
        return updateReportStatus(id, "FLAGGED");
    }

    private FraudReportResponse updateReportStatus(Long id, String status) {
        FraudReport report = fraudReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fraud report not found"));

        String oldStatus = report.getStatus();
        report.setStatus(status);
        FraudReport saved = fraudReportRepository.save(report);

        auditEventRepository.save(FraudAuditEvent.builder()
                .eventType("REPORT_STATUS_UPDATED")
                .entityId(saved.getId().toString())
                .details("Report status updated from " + oldStatus + " to " + status)
                .build());

        return fraudMapper.toReportResponse(saved);
    }
}
