package uk.gov.digital.ho.pttg.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.pttg.IpsStatisticsService;
import uk.gov.digital.ho.pttg.application.LogEvent;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static uk.gov.digital.ho.pttg.IpsStatisticsService.NO_STATISTICS;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@RunWith(MockitoJUnitRunner.class)
public class IpsStatisticsResourceTest {

    private static final LocalDate ANY_DATE = LocalDate.now();

    @Mock
    private IpsStatisticsService mockStatisticsService;
    @Mock
    private Appender<ILoggingEvent> mockAppender;

    private ArgumentCaptor<LoggingEvent> logCaptor;

    private IpsStatisticsResource resource;
    private static final int ANY_INT = 5;

    @Before
    public void setUp() {
        resource = new IpsStatisticsResource(mockStatisticsService);

        Logger logger = (Logger) LoggerFactory.getLogger(IpsStatisticsResource.class);
        logger.addAppender(mockAppender);

        logCaptor = ArgumentCaptor.forClass(LoggingEvent.class);
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
        LocalDate someFromDate = LocalDate.parse("2019-01-01");
        LocalDate someToDate = LocalDate.parse("2019-01-31");
        resource.getIpsStatistics(someFromDate, someToDate);

        then(mockAppender).should(atLeastOnce()).doAppend(logCaptor.capture());

        LoggingEvent loggingEvent = getLoggingEvent(PTTG_AUDIT_GET_IPS_STATS_REQUEST_RECEIVED);
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.INFO);
        assertThat(loggingEvent.getFormattedMessage()).contains("Request", "IPS Statistics", someFromDate.toString(), someToDate.toString());
    }

    @Test
    public void getIpsStatistics_statsFound_logReturn() {
        IpsStatistics someStatistics = new IpsStatistics(ANY_DATE, ANY_DATE, ANY_INT, ANY_INT, ANY_INT, ANY_INT);
        given(mockStatisticsService.getIpsStatistics(any(), any())).willReturn(someStatistics);

        resource.getIpsStatistics(ANY_DATE, ANY_DATE);

        then(mockAppender).should(atLeastOnce()).doAppend(logCaptor.capture());

        LoggingEvent loggingEvent = getLoggingEvent(PTTG_AUDIT_GET_IPS_STATS_RESPONSE_SUCCESS);
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.INFO);
        assertThat(loggingEvent.getFormattedMessage()).contains("Returned", "IPS Statistics");
    }

    @Test
    public void getStatistics_noStatsFound_logReturn() {
        given(mockStatisticsService.getIpsStatistics(any(), any())).willReturn(NO_STATISTICS);

        resource.getIpsStatistics(ANY_DATE, ANY_DATE);

        then(mockAppender).should(atLeastOnce()).doAppend(logCaptor.capture());

        LoggingEvent loggingEvent = getLoggingEvent(PTTG_AUDIT_GET_IPS_STATS_NOT_FOUND);
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.WARN);
        assertThat(loggingEvent.getFormattedMessage()).contains("IPS Statistics", "not found");
    }

    @Test
    public void storeIpsStatistics_givenStats_store() {
        IpsStatistics someStatistics = new IpsStatistics(ANY_DATE, ANY_DATE, ANY_INT, ANY_INT, ANY_INT, ANY_INT);

        resource.storeIpsStatistics(someStatistics);

        then(mockStatisticsService).should().storeIpsStatistics(someStatistics);
    }

    @Test
    public void storeIpsStatistics_givenStats_logEntry() {
        LocalDate someFromDate = LocalDate.parse("2019-04-01");
        LocalDate someToDate = LocalDate.parse("2019-04-30");
        IpsStatistics someStatistics = new IpsStatistics(someFromDate, someToDate, ANY_INT, ANY_INT, ANY_INT, ANY_INT);

        resource.storeIpsStatistics(someStatistics);

        then(mockAppender).should(atLeastOnce()).doAppend(logCaptor.capture());

        LoggingEvent loggingEvent = getLoggingEvent(PTTG_AUDIT_STORE_IPS_STATS_REQUEST_RECEIVED);
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.INFO);
        assertThat(loggingEvent.getFormattedMessage()).contains("Request", "store", "IPS Statistics", someToDate.toString(), someFromDate.toString());
    }

    @Test
    public void storeIpsStatistics_anyStats_logReturn() {
        IpsStatistics anyStatistics = new IpsStatistics(ANY_DATE, ANY_DATE, ANY_INT, ANY_INT, ANY_INT, ANY_INT);

        resource.storeIpsStatistics(anyStatistics);

        then(mockAppender).should(atLeastOnce()).doAppend(logCaptor.capture());

        LoggingEvent loggingEvent = getLoggingEvent(PTTG_AUDIT_STORE_IPS_STATS_RESPONSE_SUCCESS);
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.INFO);
        assertThat(loggingEvent.getFormattedMessage()).contains("Success", "store", "IPS Statistics");
    }

    public LoggingEvent getLoggingEvent(LogEvent logEvent) {
        ObjectAppendingMarker marker = new ObjectAppendingMarker("event_id", logEvent);
        return logCaptor.getAllValues().stream()
                        .filter(log -> ArrayUtils.contains(log.getArgumentArray(), marker))
                        .findFirst()
                        .orElseThrow(AssertionError::new);
    }
}