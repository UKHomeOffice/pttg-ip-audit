package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.pttg.AuditService;

import java.util.List;

@RestController
@Slf4j
public class AuditResource {

    private final AuditService auditService;

    public AuditResource(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/audit")
    public List<AuditRecord> retrieveAllAuditData() {

        log.info("Audit records requested");

        List<AuditRecord> auditRecords = auditService.getAllAuditData();

        log.info(String.format("%d audit records found", auditRecords.size()));

        return auditRecords;
    }

    @PostMapping("/audit")
    public void recordAuditEntry(@RequestBody AuditableData auditableData) {
        log.info("Audit data for correlation id {}", auditableData.getCorrelationId());
        auditService.add(auditableData);
        log.info("Audited data for correlation id {}", auditableData.getCorrelationId());
    }

}