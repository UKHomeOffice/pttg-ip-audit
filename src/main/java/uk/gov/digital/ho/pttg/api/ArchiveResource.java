package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.pttg.ArchiveService;

import java.time.LocalDate;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.pttg.api.RequestData.REQUEST_DURATION_MS;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@RestController
@Slf4j
public class ArchiveResource {

    private ArchiveService archiveService;
    private RequestData requestData;

    public ArchiveResource(ArchiveService archiveService, RequestData requestData) {
        this.archiveService = archiveService;
        this.requestData = requestData;
    }

    @PostMapping("/archive/{date}")
    @ResponseStatus(value = HttpStatus.OK)
    public void archiveResult(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody ArchiveRequest archiveRequest
    ) {

        log.info("Requested archiveResult for date {} with details {}",
                date,
                archiveRequest,
                value(EVENT, PTTG_AUDIT_ARCHIVE_RESULT_REQUEST_RECEIVED));

        archiveService.archiveResult(date, archiveRequest.result(), archiveRequest.eventIds(), archiveRequest.lastArchiveDate());

        log.info("ArchiveResult request completed successfully",
                value(EVENT, PTTG_AUDIT_ARCHIVE_RESULT_RESPONSE_SUCCESS),
                value(REQUEST_DURATION_MS, requestData.calculateRequestDuration())
                );
    }
}
