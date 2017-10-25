package uk.gov.digital.ho.pttg;

import org.json.JSONObject;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.pttg.alert.AppropriateUsageChecker;
import uk.gov.digital.ho.pttg.alert.sysdig.SuspectUsage;
import uk.gov.digital.ho.pttg.api.AuditRecord;
import uk.gov.digital.ho.pttg.api.AuditableData;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuditService {

    private final AuditEntryJpaRepository repository;
    private final AppropriateUsageChecker appropriateUsageChecker;

    public AuditService(AuditEntryJpaRepository repository, AppropriateUsageChecker appropriateUsageChecker) {
        this.repository = repository;
        this.appropriateUsageChecker = appropriateUsageChecker;
    }

    @Transactional
    public void add(AuditableData auditableData) {
        SuspectUsage suspectUsage = appropriateUsageChecker.precheck();

        AuditEntry auditEntry = transformToAuditEntry(auditableData);

        repository.save(auditEntry);

        appropriateUsageChecker.postcheck(suspectUsage);
    }

    public List<AuditRecord> getAllAuditData(Pageable pageable) {
        List<AuditEntry> auditEntries = repository.findAllByOrderByTimestampDesc(pageable);

        return auditEntries.stream()
                            .map(this::transformToAuditRecord)
                            .collect(Collectors.toList());
    }

    AuditRecord transformToAuditRecord(AuditEntry auditEntry) {
        JSONObject jsonObject = new JSONObject(auditEntry.getDetail());

        return new AuditRecord(auditEntry.getCorrelationId(),
                                auditEntry.getTimestamp(),
                                auditEntry.getUserId(),
                                auditEntry.getType(),
                                jsonObject.toMap(),
                                jsonObject.optString("nino", null));
    }

    AuditEntry transformToAuditEntry(AuditableData auditableData) {
        return new AuditEntry(auditableData.getEventId(),
                                auditableData.getTimestamp(),
                                auditableData.getSessionId(),
                                auditableData.getCorrelationId(),
                                auditableData.getUserId(),
                                auditableData.getDeploymentName(),
                                auditableData.getDeploymentNamespace(),
                                auditableData.getEventType(),
                                auditableData.getData());
    }

}
