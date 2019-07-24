package uk.gov.digital.ho.pttg;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.pttg.api.IpsStatistics;
import uk.gov.digital.ho.pttg.application.IpsStatisticsException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@AllArgsConstructor
@Slf4j
class IpsStatisticsService {

    static final IpsStatistics NO_STATISTICS = new IpsStatistics(LocalDate.MIN, LocalDate.MIN, Integer.MIN_VALUE,
                                                                 Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

    private AuditEntryJpaRepository repository;
    private ObjectMapper objectMapper;

    IpsStatistics getIpsStatistics(LocalDate fromDate, LocalDate toDate) {
        List<AuditEntry> auditEntries = repository.findAllIpsStatistics();

        List<IpsStatistics> statisticsForDate = auditEntries.stream()
                                                            .map(this::extractIpsStatistics)
                                                            .filter(stats -> sameDates(stats, fromDate, toDate))
                                                            .collect(Collectors.toList());

        if (statisticsForDate.size() > 1) {
            log.error("Multiple IPS Statistics found for fromDate={} and toDate={}", fromDate, toDate, value(EVENT, PTTG_AUDIT_IPS_STATS_MULTIPLE_FOUND));
            throw new IpsStatisticsException("Multiple IPS Statistics found");
        }
        if (statisticsForDate.isEmpty()) {
            return NO_STATISTICS;
        }
        return statisticsForDate.get(0);
    }

    private IpsStatistics extractIpsStatistics(AuditEntry auditEntry) {
        try {
            return objectMapper.readValue(auditEntry.getDetail(), IpsStatistics.class);
        } catch (IOException e) {
            log.error("Malformed IPS Statistics entry found - {}", auditEntry.getDetail(), value(EVENT, PTTG_AUDIT_IPS_STATS_MALFORMED));
            throw new IpsStatisticsException("Malformed IPS Statistics");
        }
    }

    private boolean sameDates(IpsStatistics ipsStatistics, LocalDate fromDate, LocalDate toDate) {
        return ipsStatistics.fromDate().equals(fromDate) && ipsStatistics.toDate().equals(toDate);
    }
}
