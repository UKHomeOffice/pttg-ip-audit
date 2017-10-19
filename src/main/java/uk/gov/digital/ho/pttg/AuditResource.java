package uk.gov.digital.ho.pttg;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.pttg.dto.AuditRecord;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Log
public class AuditResource {

    final AuditEntryJpaRepository repository;
    final ObjectMapper objectMapper;

    private static final TypeReference<Map<String, Object>> detailTypeRef = new TypeReference<Map<String, Object>>() {
    };

    public AuditResource(AuditEntryJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @RequestMapping(value = "/audit", method = RequestMethod.GET)
    public List<AuditRecord> allAudit() {

        log.info("Audit records requested");
        List<AuditEntry> auditEntries = repository.findAllByOrderByTimestampDesc();
        log.info(String.format("%d audit records found", auditEntries.size()));

        return auditEntries.stream()
                            .map(this::toAuditRecord)
                            .collect(Collectors.toList());
    }

    private AuditRecord toAuditRecord(AuditEntry auditEntry) {
        try {
            Map<String, Object> detail = objectMapper.readValue(auditEntry.getDetail(), detailTypeRef);
            return new AuditRecord(auditEntry.getCorrelationId(), auditEntry.getTimestamp(), auditEntry.getUserId(), auditEntry.getType(), detail, (String) detail.get("nino"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}