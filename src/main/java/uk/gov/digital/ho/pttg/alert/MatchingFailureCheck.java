package uk.gov.digital.ho.pttg.alert;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.AuditEntry;
import uk.gov.digital.ho.pttg.AuditEntryJpaRepository;
import uk.gov.digital.ho.pttg.AuditEventType;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class MatchingFailureCheck {
    private final int matchFailureThreshold;
    private final String namespace;

    public MatchingFailureCheck(@Value("${alert.match.failure.threshold}") int matchFailureThreshold, @Value("${auditing.deployment.namespace}") String namespace) {
        this.matchFailureThreshold = matchFailureThreshold;
        this.namespace = namespace;
    }

    MatchingFailureUsage check(AuditEntryJpaRepository repository) {
        List<AuditEntry> auditEntries = repository.getEntriesBetweenDates(
            LocalDateTime.now().minusMinutes(60),
            LocalDateTime.now(),
            AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
            namespace);

        long notFoundInPeriod = auditEntries.stream().filter(AuditEntry::isNotFoundEvent).count();
        return new MatchingFailureUsage(notFoundInPeriod > matchFailureThreshold ? notFoundInPeriod : 0);
    }
}
