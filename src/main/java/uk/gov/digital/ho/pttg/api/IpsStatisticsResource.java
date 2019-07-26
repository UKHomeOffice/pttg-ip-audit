package uk.gov.digital.ho.pttg.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.pttg.IpsStatisticsService;

import java.time.LocalDate;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.pttg.IpsStatisticsService.NO_STATISTICS;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@AllArgsConstructor
@Slf4j
public class IpsStatisticsResource {

    private IpsStatisticsService service;

    public ResponseEntity<IpsStatistics> getIpsStatistics(LocalDate fromDate, LocalDate toDate) {
        log.info("Requested IPS Statistics with fromDate={} and toDate={}", fromDate, toDate,
                 value(EVENT, PTTG_AUDIT_GET_IPS_STATS_REQUEST_RECEIVED));

        IpsStatistics ipsStatistics = service.getIpsStatistics(fromDate, toDate);
        if(ipsStatistics == NO_STATISTICS) {
            log.warn("IPS Statistics not found", value(EVENT, PTTG_AUDIT_GET_IPS_STATS_NOT_FOUND));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        log.info("Returned IPS Statistics", value(EVENT, PTTG_AUDIT_GET_IPS_STATS_RESPONSE_SUCCESS));
        return new ResponseEntity<>(ipsStatistics, HttpStatus.OK);
    }

    public void storeIpsStatistics(IpsStatistics ipsStatistics) {
        log.info("Request to store IPS Statistics for fromDate={} and toDate={}", ipsStatistics.fromDate(), ipsStatistics.toDate(),
                 value(EVENT, PTTG_AUDIT_STORE_IPS_STATS_REQUEST_RECEIVED));
        service.storeIpsStatistics(ipsStatistics);
        log.info("Successfully stored IPS Statistics", value(EVENT, PTTG_AUDIT_STORE_IPS_STATS_RESPONSE_SUCCESS));
    }
}
