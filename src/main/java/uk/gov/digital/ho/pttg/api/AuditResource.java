package uk.gov.digital.ho.pttg.api;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.digital.ho.pttg.api.RequestData.REQUEST_DURATION_MS;
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.PTTG_AUDIT_REQUEST_COMPLETED;
import static uk.gov.digital.ho.pttg.application.LogEvent.PTTG_AUDIT_REQUEST_RECEIVED;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.pttg.AuditService;

@RestController
@Slf4j
public class AuditResource {

    private final AuditService auditService;
    private final RequestData requestData;

    public AuditResource(AuditService auditService, RequestData requestData) {
        this.auditService = auditService;
        this.requestData = requestData;
    }

    @PostMapping(value = "/audit", consumes = APPLICATION_JSON_VALUE)
    public void recordAuditEntry(@RequestBody AuditableData auditableData) {

        log.info("Audit request {} received for correlation id {}",
                    auditableData.getEventType(),
                    auditableData.getCorrelationId(),
                    value(EVENT, PTTG_AUDIT_REQUEST_RECEIVED));

        auditService.add(auditableData);

        log.info("Audit request {} completed for correlation id {}",
                    auditableData.getEventType(),
                    auditableData.getCorrelationId(),
                    value(EVENT, PTTG_AUDIT_REQUEST_COMPLETED),
                    value(REQUEST_DURATION_MS, requestData.calculateRequestDuration()));
    }
}
