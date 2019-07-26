package uk.gov.digital.ho.pttg.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.pttg.IpsStatisticsService;

import java.time.LocalDate;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.digital.ho.pttg.IpsStatisticsService.NO_STATISTICS;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@AllArgsConstructor
@Slf4j
@RestController
public class IpsStatisticsResource {

    private IpsStatisticsService service;

    @GetMapping(value = "/ipsstatistics", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<IpsStatistics> getIpsStatistics(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("Requested IPS Statistics with fromDate={} and toDate={}", fromDate, toDate,
                 value(EVENT, PTTG_AUDIT_GET_IPS_STATS_REQUEST_RECEIVED));

        IpsStatistics ipsStatistics = service.getIpsStatistics(fromDate, toDate);

        return responseEntity(ipsStatistics);
    }

    @PostMapping(value = "/ipsstatistics")
    public void storeIpsStatistics(@RequestBody IpsStatistics ipsStatistics) {
        log.info("Request to store IPS Statistics for fromDate={} and toDate={}", ipsStatistics.fromDate(), ipsStatistics.toDate(),
                 value(EVENT, PTTG_AUDIT_STORE_IPS_STATS_REQUEST_RECEIVED));
        service.storeIpsStatistics(ipsStatistics);
        log.info("Successfully stored IPS Statistics", value(EVENT, PTTG_AUDIT_STORE_IPS_STATS_RESPONSE_SUCCESS));
    }

    private ResponseEntity<IpsStatistics> responseEntity(IpsStatistics ipsStatistics) {
        if (ipsStatistics == NO_STATISTICS) {
            log.warn("IPS Statistics not found", value(EVENT, PTTG_AUDIT_GET_IPS_STATS_NOT_FOUND));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        log.info("Returned IPS Statistics", value(EVENT, PTTG_AUDIT_GET_IPS_STATS_RESPONSE_SUCCESS));
        return new ResponseEntity<>(ipsStatistics, HttpStatus.OK);
    }
}
