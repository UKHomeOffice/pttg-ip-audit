package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.pttg.AuditEventType;
import uk.gov.digital.ho.pttg.AuditHistoryService;

import java.time.LocalDate;
import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.digital.ho.pttg.api.RequestData.REQUEST_DURATION_MS;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@RestController
@Slf4j
public class AuditHistoryResource {

    private AuditHistoryService auditHistoryService;
    private RequestData requestData;

    public AuditHistoryResource(AuditHistoryService auditHistoryService, RequestData requestData) {
        this.auditHistoryService = auditHistoryService;
        this.requestData = requestData;
    }

    @GetMapping(value = "/history", produces = APPLICATION_JSON_VALUE)
    public List<AuditRecord> retrieveAuditHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam List<AuditEventType> eventTypes,
            Pageable pageable
    ) {

        log.info("Requested Audit History for events {} up to end date {} with pageable of {}",
                eventTypes,
                toDate,
                pageable,
                value(EVENT, PTTG_AUDIT_HISTORY_REQUEST_RECEIVED));
        toDate = useDefaultIfNull(toDate);

        List<AuditRecord> result = auditHistoryService.getAuditHistory(toDate, eventTypes, pageable);

        log.info("Returned {} audit record(s) for history request",
                result.size(),
                value(EVENT, PTTG_AUDIT_HISTORY_RESPONSE_SUCCESS),
                value(REQUEST_DURATION_MS, requestData.calculateRequestDuration())
        );

        return result;
    }

    @GetMapping(value = "/correlationIds", produces = APPLICATION_JSON_VALUE)
    public List<String> getAllCorrelationIds(@RequestParam List<AuditEventType> eventTypes,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        logGetAllCorrelationIdEntry(eventTypes, toDate);
        List<String> correlationIds = getCorrelationIdsFromService(eventTypes, toDate);
        logCorrelationIdCount(correlationIds);
        return correlationIds;
    }

    @GetMapping(value = "/historyByCorrelationId", produces = APPLICATION_JSON_VALUE)
    public List<AuditRecord> getRecordsForCorrelationId(
            @RequestParam String correlationId, @RequestParam List<AuditEventType> eventTypes) {

        log.info("Requested audit records for correlationID {} and events {}", correlationId, eventTypes,
                value(EVENT, PTTG_AUDIT_HISTORY_BY_CORRELATION_ID_REQUEST_RECEIVED));

        List<AuditRecord> records = auditHistoryService.getRecordsForCorrelationId(correlationId, eventTypes);

        log.info("Returned {} audit records for correlation ID {}", records.size(), correlationId,
                value(EVENT, PTTG_AUDIT_HISTORY_BY_CORRELATION_ID_RESPONSE_SUCCESS),
                value(REQUEST_DURATION_MS, requestData.calculateRequestDuration()));

        return records;
    }

    private void logGetAllCorrelationIdEntry(List<AuditEventType> eventTypes, LocalDate toDate) {
        if (toDate == null) {
            log.info("Requested all correlation ids for events {}", eventTypes, value(EVENT, PTTG_AUDIT_HISTORY_CORRELATION_IDS_REQUEST_RECEIVED));
        } else {
            log.info("Requested all correlation ids for events {} up to {}", eventTypes, toDate, value(EVENT, PTTG_AUDIT_HISTORY_CORRELATION_IDS_REQUEST_RECEIVED));
        }
    }

    private List<String> getCorrelationIdsFromService(List<AuditEventType> eventTypes, LocalDate toDate) {
        if (toDate == null) {
            return auditHistoryService.getAllCorrelationIds(eventTypes);
        }
        return auditHistoryService.getAllCorrelationIds(eventTypes, toDate);
    }

    private void logCorrelationIdCount(List<String> correlationIds) {
        log.info("Returning {} correlation IDs for all correlation ID request", correlationIds.size(),
                 value(EVENT, PTTG_AUDIT_HISTORY_CORRELATION_IDS_RESPONSE_SUCCESS),
                 value(REQUEST_DURATION_MS, requestData.calculateRequestDuration()));
    }

    private LocalDate useDefaultIfNull(LocalDate toDate) {
        return toDate != null ? toDate : LocalDate.now();
    }
}
