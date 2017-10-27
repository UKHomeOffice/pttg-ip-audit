package uk.gov.digital.ho.pttg.alert;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.AuditEntry;
import uk.gov.digital.ho.pttg.AuditEntryJpaRepository;
import uk.gov.digital.ho.pttg.AuditEventType;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class MatchingFailureCheck {

    private final Clock clock;
    private final AuditEntryJpaRepository repository;
    private final int matchFailureThreshold;
    private final String namespace;

    public MatchingFailureCheck(Clock clock,
                                AuditEntryJpaRepository repository,
                                @Value("${alert.match.failure.threshold}") int matchFailureThreshold,
                                @Value("${auditing.deployment.namespace}") String namespace) {
        this.clock = clock;
        this.repository = repository;
        this.matchFailureThreshold = matchFailureThreshold;
        this.namespace = namespace;
    }

    MatchingFailureUsage check() {
        List<AuditEntry> auditEntries = repository.getEntriesBetweenDates(
            LocalDateTime.now(clock).minusMinutes(60),
            LocalDateTime.now(clock),
            AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
            namespace);

        long notFoundInPeriod = auditEntries.stream().filter(AuditEntry::isNotFoundEvent).count();
        return new MatchingFailureUsage(notFoundInPeriod > matchFailureThreshold ? notFoundInPeriod : 0);
    }
}
