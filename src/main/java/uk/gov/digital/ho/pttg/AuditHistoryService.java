package uk.gov.digital.ho.pttg;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.api.AuditRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class AuditHistoryService {

    public List<AuditRecord> getAuditHistory(LocalDate toDate, List<AuditEventType> eventTypes) {
        return new ArrayList<>();
    }
}
