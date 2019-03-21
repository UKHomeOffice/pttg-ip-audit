package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
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
    public List<AuditRecord> retrieveAuditHistory(@RequestParam LocalDate toDate, @RequestParam List<AuditEventType> eventTypes) {

        log.info("Requested Audit History for events {} up to end date {}",
                eventTypes,
                toDate,
                value(EVENT, PTTG_AUDIT_HISTORY_REQUEST_RECEIVED));

        List<AuditRecord> result = auditHistoryService.getAuditHistory(toDate, eventTypes);

        log.info("Returned {} audit record(s) for history request",
                result.size(),
                value(EVENT, PTTG_AUDIT_HISTORY_RESPONSE_SUCCESS),
                value(REQUEST_DURATION_MS, requestData.calculateRequestDuration())
        );

        return result;
    }
}
