package uk.gov.digital.ho.pttg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.pttg.api.IpsStatistics;
import uk.gov.digital.ho.pttg.application.IpsStatisticsException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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

        if (statisticsForDate.isEmpty()) {
            return NO_STATISTICS;
        }

        checkForSingleMatch(fromDate, toDate, statisticsForDate);
        return statisticsForDate.get(0);
    }

    void storeIpsStatistics(IpsStatistics ipsStatistics) {
        if (repository.findAllIpsStatistics().stream()
                      .map(this::extractIpsStatistics)
                      .anyMatch(stats -> sameDates(stats, ipsStatistics.fromDate(), ipsStatistics.toDate()))) {
            log.error("Statistics already exist for fromDate={} and toDate={}", ipsStatistics.fromDate(), ipsStatistics.toDate(),
                      value(EVENT, PTTG_AUDIT_IPS_STATS_DUPLICATION_ATTEMPT));
            throw new IpsStatisticsException("Statistics already exist for the date range");
        }

        try {
            repository.save(createAuditEvent(ipsStatistics));
        } catch (JsonProcessingException e) {
            log.error("JSON parse error for IpsStatistics", value(EVENT, PTTG_AUDIT_IPS_STATS_PARSE_ERROR));
            throw new IpsStatisticsException("JSON parse error");
        }
    }

    private IpsStatistics extractIpsStatistics(AuditEntry auditEntry) {
        try {
            return objectMapper.readValue(auditEntry.getDetail(), IpsStatistics.class);
        } catch (IOException e) {
            log.error("Malformed IPS Statistics entry found - {}", auditEntry.getDetail(), value(EVENT, PTTG_AUDIT_IPS_STATS_MALFORMED));
            throw new IpsStatisticsException("Malformed IPS Statistics");
        }
    }

    private void checkForSingleMatch(LocalDate fromDate, LocalDate toDate, List<IpsStatistics> statisticsForDate) {
        if (statisticsForDate.size() > 1) {
            log.error("Multiple IPS Statistics found for fromDate={} and toDate={}", fromDate, toDate, value(EVENT, PTTG_AUDIT_IPS_STATS_MULTIPLE_FOUND));
            throw new IpsStatisticsException("Multiple IPS Statistics found");
        }
    }

    private AuditEntry createAuditEvent(IpsStatistics ipsStatistics) throws JsonProcessingException {
        return new AuditEntry(UUID.randomUUID().toString(),
                              LocalDateTime.now(),
                              "",
                              UUID.randomUUID().toString(),
                              "Audit Service",
                              "",
                              "",
                              AuditEventType.IPS_STATISTICS,
                              objectMapper.writeValueAsString(ipsStatistics));
    }

    private boolean sameDates(IpsStatistics ipsStatistics, LocalDate fromDate, LocalDate toDate) {
        return ipsStatistics.fromDate().equals(fromDate) && ipsStatistics.toDate().equals(toDate);
    }
}
