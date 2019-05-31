package uk.gov.digital.ho.pttg;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.api.AuditRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuditHistoryService {

    private AuditEntryJpaRepository repository;

    public AuditHistoryService(AuditEntryJpaRepository repository) {
        this.repository = repository;
    }

    public List<AuditRecord> getAuditHistory(LocalDate toDate, List<AuditEventType> eventTypes, Pageable pageable) {
        LocalDateTime toDateEod = toDate.atTime(23, 59, 59, 999);
        List<AuditEntry> entries = repository.findAuditHistory(toDateEod, eventTypes, pageable);
        return entries.stream()
                .map(AuditService::transformToAuditRecord)
                .collect(Collectors.toList());
    }

    public List<String> getAllCorrelationIds(List<AuditEventType> eventTypes){
        return repository.getAllCorrelationIds(eventTypes);
    }
}
