package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.pttg.ArchiveService;

import java.time.LocalDate;
import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@RestController
@Slf4j
public class ArchiveResource {

    private final ArchiveService archiveService;

    public ArchiveResource(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @GetMapping(value = "/archived", produces = APPLICATION_JSON_VALUE)
    public List<ArchivedResult> getArchivedResults(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        log.info("Request received for archived results between {} and {}", fromDate, toDate,
                value(EVENT, PTTG_AUDIT_ARCHIVE_REQUEST_RECEIVED));

        List<ArchivedResult> archivedResults = archiveService.getArchivedResults(fromDate, toDate);

        log.info("Returned response with {} archived results", archivedResults.size(),
                value(EVENT, PTTG_AUDIT_ARCHIVE_RESPONSE_SUCCESS));

        return archivedResults;
    }
}
