package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.pttg.ArchiveService;

@RestController
@Slf4j
public class ArchiveResource {

    private ArchiveService archiveService;
    private RequestData requestData;

    public ArchiveResource(ArchiveService archiveService, RequestData requestData) {
        this.archiveService = archiveService;
        this.requestData = requestData;
    }

    @PostMapping("/nino/{nino}/archive/{result}")
    @ResponseStatus(value = HttpStatus.OK)
    public void archiveNino(
            @PathVariable String nino,
            @PathVariable String result,
            @RequestBody ArchiveRequest archiveRequest
    ) {

    }
}
