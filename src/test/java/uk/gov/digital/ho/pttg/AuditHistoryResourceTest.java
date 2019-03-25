package uk.gov.digital.ho.pttg;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import uk.gov.digital.ho.pttg.api.AuditHistoryResource;
import uk.gov.digital.ho.pttg.api.AuditRecord;
import uk.gov.digital.ho.pttg.api.RequestData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_REQUEST;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE;

@RunWith(MockitoJUnitRunner.class)
public class AuditHistoryResourceTest {

    @Mock private Appender<ILoggingEvent> mockAppender;
    @Mock private AuditHistoryService mockHistoryService;
    @Mock private RequestData mockRequestData;

    private AuditHistoryResource historyResource;

    private static final AuditRecord AUDIT_RECORD = new AuditRecord("some id",
            LocalDateTime.of(2017, 12, 8, 0, 0),
            "some email",
            AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_REQUEST,
            ImmutableMap.of("some key", "some value"),
            "some nino"
    );

    @Before
    public void setUp() {
        historyResource = new AuditHistoryResource(mockHistoryService, mockRequestData);
        Logger rootLogger = (Logger) LoggerFactory.getLogger(AuditHistoryResource.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);
    }

    @Test
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void retrieveAuditHistory_callsAuditHistoryService() {
        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
        when(mockHistoryService.getAuditHistory(LocalDate.now(), eventTypes)).thenReturn(Arrays.asList(AUDIT_RECORD));

        historyResource.retrieveAuditHistory(LocalDate.now(), eventTypes);

        verify(mockHistoryService).getAuditHistory(LocalDate.now(), eventTypes);
    }

    @Test
    public void retrieveAuditHistory_returnsResultFromAuditHistoryService() {
        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
        List<AuditRecord> expected = Arrays.asList(AUDIT_RECORD);
        when(mockHistoryService.getAuditHistory(LocalDate.now(), eventTypes)).thenReturn(expected);

        List<AuditRecord> result = historyResource.retrieveAuditHistory(LocalDate.now(), eventTypes);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void retrieveAuditHistory_logsRequestParameters() {
        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
        when(mockHistoryService.getAuditHistory(LocalDate.now(), eventTypes)).thenReturn(Arrays.asList(AUDIT_RECORD));

        historyResource.retrieveAuditHistory(LocalDate.now(), eventTypes);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            String expectedLogMessage = String.format("Requested Audit History for events [%s, %s] up to end date %s",
                    INCOME_PROVING_FINANCIAL_STATUS_REQUEST.name(),
                    INCOME_PROVING_FINANCIAL_STATUS_RESPONSE.name(),
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyy-MM-dd")));
            return loggingEvent.getFormattedMessage().equals(expectedLogMessage) &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void retrieveAuditHistory_logsResult() {
        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
        when(mockHistoryService.getAuditHistory(LocalDate.now(), eventTypes)).thenReturn(Arrays.asList(AUDIT_RECORD));

        historyResource.retrieveAuditHistory(LocalDate.now(), eventTypes);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Returned 1 audit record(s) for history request") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("request_duration_ms");
        }));
    }



}