package uk.gov.digital.ho.pttg;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
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
    final AuditRepository repo;
    final ObjectMapper objectMapper;
    private static final TypeReference<Map<String, Object>> detailTypeRef = new TypeReference<Map<String, Object>>() {
    };

    @Autowired
    public AuditResource(AuditRepository repo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper;
    }

    @RequestMapping(value = "/audit", method = RequestMethod.GET)
    public List<AuditRecord> allAudit() {
        log.info("Audit records requested");
        List<Audit> auditList = repo.findAllByOrderByTimestampDesc();
        log.info(String.format("%d audit records found", auditList.size()));

        return auditList.stream().map(this::toAuditRecord).collect(Collectors.toList());
    }

    private AuditRecord toAuditRecord(Audit audit) {
        try {
            Map<String, Object> detail = objectMapper.readValue(audit.getDetail(), detailTypeRef);
            return new AuditRecord(audit.getUuid(), audit.getTimestamp(), audit.getUserId(), audit.getType().name(), detail, (String) detail.get("nino"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}