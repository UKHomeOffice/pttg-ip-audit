package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.pttg.AuditService;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Slf4j
public class AuditResource {

    private final AuditService auditService;

    public AuditResource(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping(value = "/audit", produces = APPLICATION_JSON_VALUE)
    public List<AuditRecord> retrieveAllAuditData(Pageable pageable) {

        log.info("Audit records requested, {}", pageable);

        List<AuditRecord> auditRecords = auditService.getAllAuditData(pageable);

        log.info("{} audit records found", auditRecords.size());

        return auditRecords;
    }

    @PostMapping(value = "/audit", consumes = APPLICATION_JSON_VALUE)
    public void recordAuditEntry(@RequestBody AuditableData auditableData) {
        log.info("Audit {} for correlation id {}", auditableData.getEventType(), auditableData.getCorrelationId());
        auditService.add(auditableData);
        log.info("Audited {} for correlation id {}", auditableData.getEventType(), auditableData.getCorrelationId());
    }

}