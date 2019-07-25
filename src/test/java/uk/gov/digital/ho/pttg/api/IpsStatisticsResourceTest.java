package uk.gov.digital.ho.pttg.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.pttg.IpsStatisticsService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static uk.gov.digital.ho.pttg.IpsStatisticsService.NO_STATISTICS;

@RunWith(MockitoJUnitRunner.class)
public class IpsStatisticsResourceTest {

    private static final LocalDate ANY_DATE = LocalDate.now();

    @Mock
    private IpsStatisticsService mockStatisticsService;

    private IpsStatisticsResource resource;
    private static final int ANY_INT = 5;

    @Before
    public void setUp() {
        resource = new IpsStatisticsResource(mockStatisticsService);
    }

    @Test
    public void getIpsStatistics_givenDates_callService() {
        LocalDate someFromDate = LocalDate.parse("2019-07-01");
        LocalDate someToDate = LocalDate.parse("2019-07-31");

        resource.getIpsStatistics(someFromDate, someToDate);

        then(mockStatisticsService).should().getIpsStatistics(someFromDate, someToDate);
    }

    @Test
    public void getIpsStatistics_statsFromService_returned() {
        IpsStatistics expectedStatistics = new IpsStatistics(ANY_DATE, ANY_DATE, ANY_INT, ANY_INT, ANY_INT, ANY_INT);
        given(mockStatisticsService.getIpsStatistics(any(), any())).willReturn(expectedStatistics);

        ResponseEntity<IpsStatistics> response = resource.getIpsStatistics(ANY_DATE, ANY_DATE);

        assertThat(response.getBody()).isEqualTo(expectedStatistics);
    }

    @Test
    public void getIpsStatistics_noStatsFound_404NotFound() {
        given(mockStatisticsService.getIpsStatistics(any(), any())).willReturn(NO_STATISTICS);

        ResponseEntity<IpsStatistics> response = resource.getIpsStatistics(ANY_DATE, ANY_DATE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getIpsStatistics_noStatsFound_noBody() {
        given(mockStatisticsService.getIpsStatistics(any(), any())).willReturn(NO_STATISTICS);

        ResponseEntity<IpsStatistics> response = resource.getIpsStatistics(ANY_DATE, ANY_DATE);

        assertThat(response.getBody()).isNull();
    }

    @Test
    public void getIpsStatistics_givenDates_logEntry() {
        fail("TODO OJR EE-20105 2019-07-25");
    }

    @Test
    public void getIpsStatistics_statsFound_logReturn() {
        fail("TODO OJR EE-20105 2019-07-25");
    }

    @Test
    public void getStatistics_noStatsFound_logReturn() {
        fail("TODO OJR EE-20105 2019-07-25");
    }

    @Test
    public void storeIpsStatistics_givenStats_store() {
        IpsStatistics someStatistics = new IpsStatistics(ANY_DATE, ANY_DATE, ANY_INT, ANY_INT, ANY_INT, ANY_INT);

        resource.storeIpsStatistics(someStatistics);

        then(mockStatisticsService).should().storeIpsStatistics(someStatistics);
    }

    @Test
    public void storeIpsStatistics_givenStats_logEntry() {
        fail("TODO OJR EE-20105 2019-07-25");
    }

    @Test
    public void storeIpsStatistics_anyStats_logReturn() {
        fail("TODO OJR EE-20105 2019-07-25");
    }
}