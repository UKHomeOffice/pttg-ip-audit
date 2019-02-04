package uk.gov.digital.ho.pttg;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.alert.AppropriateUsageChecker;
import uk.gov.digital.ho.pttg.alert.sysdig.SuspectUsage;
import uk.gov.digital.ho.pttg.api.AuditRecord;
import uk.gov.digital.ho.pttg.api.AuditableData;

import java.util.List;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.PTTG_AUDIT_CONFIG_MISMATCH;

@Component
@Slf4j
public class AuditService {

    private final AuditEntryJpaRepository repository;
    private final AppropriateUsageChecker appropriateUsageChecker;
    private final String auditingDeploymentNamespace;

    public AuditService(AuditEntryJpaRepository repository,
                        AppropriateUsageChecker appropriateUsageChecker,
                        @Value("${auditing.deployment.namespace}") String auditingDeploymentNamespace) {
        this.repository = repository;
        this.appropriateUsageChecker = appropriateUsageChecker;
        this.auditingDeploymentNamespace = auditingDeploymentNamespace;
    }

    public void add(AuditableData auditableData) {

        AuditEntry auditEntry = transformToAuditEntry(auditableData);

        if (auditEntry.getType().isAlertable()) {
            checkNamespace(auditableData);
            SuspectUsage suspectUsage = appropriateUsageChecker.precheck();
            repository.save(auditEntry);
            appropriateUsageChecker.postcheck(suspectUsage);
        } else {
            repository.save(auditEntry);
        }

    }

    private void checkNamespace(AuditableData auditableData) {
        String eventNamespace = auditableData.getDeploymentNamespace();

        if (!eventNamespace.equals(auditingDeploymentNamespace)) {
            log.warn("Auditing Deployment Namespace of AuditEvent = '{}' does not match '{}' so no suspicious behaviour will be detected.",
                    eventNamespace, auditingDeploymentNamespace, value(EVENT, PTTG_AUDIT_CONFIG_MISMATCH));
        }
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
