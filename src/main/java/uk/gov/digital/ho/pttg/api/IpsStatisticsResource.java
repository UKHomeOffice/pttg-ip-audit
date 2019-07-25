package uk.gov.digital.ho.pttg.api;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.pttg.IpsStatisticsService;

import java.time.LocalDate;

import static uk.gov.digital.ho.pttg.IpsStatisticsService.NO_STATISTICS;

@AllArgsConstructor
public class IpsStatisticsResource {

    private IpsStatisticsService service;

    public ResponseEntity<IpsStatistics> getIpsStatistics(LocalDate fromDate, LocalDate toDate) {
        IpsStatistics ipsStatistics = service.getIpsStatistics(fromDate, toDate);
        if(ipsStatistics == NO_STATISTICS) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ipsStatistics, HttpStatus.OK);
    }

    public void storeIpsStatistics(IpsStatistics ipsStatistics) {
        service.storeIpsStatistics(ipsStatistics);
    }
}
