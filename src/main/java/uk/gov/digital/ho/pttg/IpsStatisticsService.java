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
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.PTTG_AUDIT_IPS_STATS_ERROR;

@AllArgsConstructor
@Slf4j
public class IpsStatisticsService {

    public static final IpsStatistics NO_STATISTICS = new IpsStatistics(LocalDate.MIN, LocalDate.MIN, Integer.MIN_VALUE,
                                                                        Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

    private AuditEntryJpaRepository repository;
    private ObjectMapper objectMapper;

    public IpsStatistics getIpsStatistics(LocalDate fromDate, LocalDate toDate) {
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

    public void storeIpsStatistics(IpsStatistics ipsStatistics) {
        if (isDuplicatedStatistics(ipsStatistics)) {
            handleError(String.format("Statistics already exist for fromDate=%s and toDate=%s", ipsStatistics.fromDate(), ipsStatistics.toDate()));
        }

        try {
            repository.save(createAuditEvent(ipsStatistics));
        } catch (JsonProcessingException e) {
            handleError("JSON parse error");
        }
    }

    private IpsStatistics extractIpsStatistics(AuditEntry auditEntry) {
        IpsStatistics extractedStatistics = null;
        try {
            extractedStatistics = objectMapper.readValue(auditEntry.getDetail(), IpsStatistics.class);
        } catch (IOException e) {
            handleError(String.format("Malformed IPS Statistics entry found - %s", auditEntry.getDetail()));
        }
        return extractedStatistics;
    }

    private void checkForSingleMatch(LocalDate fromDate, LocalDate toDate, List<IpsStatistics> statisticsForDate) {
        if (statisticsForDate.size() > 1) {
            handleError(String.format("Multiple IPS Statistics found for fromDate=%s and toDate=%s", fromDate, toDate));
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

    private void handleError(String message) {
        log.error(message, value(EVENT, PTTG_AUDIT_IPS_STATS_ERROR));
        throw new IpsStatisticsException(message);
    }

    private boolean isDuplicatedStatistics(IpsStatistics ipsStatistics) {
        IpsStatistics duplicatedStatistics = getIpsStatistics(ipsStatistics.fromDate(), ipsStatistics.toDate());
        return duplicatedStatistics != NO_STATISTICS;
    }

    private boolean sameDates(IpsStatistics ipsStatistics, LocalDate fromDate, LocalDate toDate) {
        return ipsStatistics.fromDate().equals(fromDate) && ipsStatistics.toDate().equals(toDate);
    }
}
