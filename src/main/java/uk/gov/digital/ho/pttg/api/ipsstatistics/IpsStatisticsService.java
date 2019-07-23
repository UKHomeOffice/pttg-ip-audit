package uk.gov.digital.ho.pttg.api.ipsstatistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import uk.gov.digital.ho.pttg.AuditEntry;
import uk.gov.digital.ho.pttg.AuditEntryJpaRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
class IpsStatisticsService {

    static final IpsStatistics NO_STATISTICS = new IpsStatistics(LocalDate.MIN, LocalDate.MIN, Integer.MIN_VALUE,
                                                                 Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

    private AuditEntryJpaRepository repository;
    private ObjectMapper objectMapper;

    IpsStatistics getIpsStatistics(LocalDate fromDate, LocalDate toDate) {
        List<AuditEntry> auditEntries = repository.findAllIpsStatistics();
        return auditEntries.stream()
                           .map(this::extractIpsStatistics)
                           .filter(stats -> sameDates(stats, fromDate, toDate))
                           .findFirst()
                           .orElse(NO_STATISTICS);
    }

    private IpsStatistics extractIpsStatistics(AuditEntry auditEntry) {
        try {
            return objectMapper.readValue(auditEntry.getDetail(), IpsStatistics.class);
        } catch (IOException e) {
            // TODO EE-20105 OJR 2019-07-23
            return NO_STATISTICS;
        }
    }

    private boolean sameDates(IpsStatistics ipsStatistics, LocalDate fromDate, LocalDate toDate) {
        return ipsStatistics.fromDate().equals(fromDate) && ipsStatistics.toDate().equals(toDate);
    }
}
