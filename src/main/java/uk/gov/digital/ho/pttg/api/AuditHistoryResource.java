package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.pttg.AuditEventType;
import uk.gov.digital.ho.pttg.AuditHistoryService;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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

        return auditHistoryService.getAuditHistory(toDate, eventTypes);
    }
}
