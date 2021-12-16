package uk.gov.digital.ho.pttg;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.PTTG_AUDIT_CONFIG_MISMATCH;
import static uk.gov.digital.ho.pttg.application.LogEvent.PTTG_AUDIT_FAILURE;
import static uk.gov.digital.ho.pttg.application.LogEvent.PTTG_AUDIT_RESPONSE_SUCCESS;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.alert.AppropriateUsageChecker;
import uk.gov.digital.ho.pttg.alert.sysdig.SuspectUsage;
import uk.gov.digital.ho.pttg.api.AuditRecord;
import uk.gov.digital.ho.pttg.api.AuditableData;

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
            addAudit(auditEntry);
            appropriateUsageChecker.postcheck(suspectUsage);
        } else {
            addAudit(auditEntry);
        }
    }

    private void addAudit(AuditEntry auditEntry) {
        try {
            repository.save(auditEntry);
            log.info("Audit request {} processed for correlation id {}",
                    auditEntry.getType(), auditEntry.getCorrelationId(), value(EVENT, PTTG_AUDIT_RESPONSE_SUCCESS));

        } catch ( DataIntegrityViolationException e) {
            log.warn("Audit exception: audit with event uuid: {} and event type: {} already exists.",
                    auditEntry.getUuid(), auditEntry.getType(), value(EVENT, PTTG_AUDIT_FAILURE));
        }
    }

    private void checkNamespace(AuditableData auditableData) {
        String eventNamespace = auditableData.getDeploymentNamespace();

        if (!eventNamespace.equals(auditingDeploymentNamespace)) {
            log.warn("Auditing Deployment Namespace of AuditEvent = '{}' does not match '{}' so no suspicious behaviour will be detected.",
                    eventNamespace, auditingDeploymentNamespace, value(EVENT, PTTG_AUDIT_CONFIG_MISMATCH));
        }
    }

    static AuditRecord transformToAuditRecord(AuditEntry auditEntry) {
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
