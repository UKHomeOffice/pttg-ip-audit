package uk.gov.digital.ho.pttg.api;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.gov.digital.ho.pttg.AuditEventType;
import uk.gov.digital.ho.pttg.AuditHistoryService;
import uk.gov.digital.ho.pttg.api.AuditHistoryResource;
import uk.gov.digital.ho.pttg.api.AuditRecord;
import uk.gov.digital.ho.pttg.api.RequestData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_REQUEST;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

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
    private static final Pageable SOME_PAGEABLE = PageRequest.of(5, 8);

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

        when(mockHistoryService.getAuditHistory(LocalDate.now(), eventTypes, SOME_PAGEABLE)).thenReturn(Arrays.asList(AUDIT_RECORD));

        historyResource.retrieveAuditHistory(LocalDate.now(), eventTypes, SOME_PAGEABLE);

        verify(mockHistoryService).getAuditHistory(LocalDate.now(), eventTypes, SOME_PAGEABLE);
    }

    @Test
    public void retrieveAuditHistory_returnsResultFromAuditHistoryService() {
        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
        List<AuditRecord> expected = Arrays.asList(AUDIT_RECORD);
        when(mockHistoryService.getAuditHistory(LocalDate.now(), eventTypes, SOME_PAGEABLE)).thenReturn(expected);

        List<AuditRecord> result = historyResource.retrieveAuditHistory(LocalDate.now(), eventTypes, SOME_PAGEABLE);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void retrieveAuditHistory_logsRequestParameters() {
        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
        when(mockHistoryService.getAuditHistory(LocalDate.now(), eventTypes, SOME_PAGEABLE)).thenReturn(Arrays.asList(AUDIT_RECORD));

        historyResource.retrieveAuditHistory(LocalDate.now(), eventTypes, SOME_PAGEABLE);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            String expectedLogMessage = String.format("Requested Audit History for events [%s, %s] up to end date %s with pageable of %s",
                    INCOME_PROVING_FINANCIAL_STATUS_REQUEST.name(),
                    INCOME_PROVING_FINANCIAL_STATUS_RESPONSE.name(),
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyy-MM-dd")),
                    SOME_PAGEABLE);
            return loggingEvent.getFormattedMessage().equals(expectedLogMessage) &&
                    loggingEvent.getArgumentArray()[3].equals(new ObjectAppendingMarker("event_id", PTTG_AUDIT_HISTORY_REQUEST_RECEIVED));
        }));
    }

    @Test
    public void retrieveAuditHistory_logsResult() {
        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
        when(mockHistoryService.getAuditHistory(LocalDate.now(), eventTypes, SOME_PAGEABLE)).thenReturn(Arrays.asList(AUDIT_RECORD));

        historyResource.retrieveAuditHistory(LocalDate.now(), eventTypes, SOME_PAGEABLE);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Returned 1 audit record(s) for history request") &&
                    loggingEvent.getArgumentArray()[1].equals(new ObjectAppendingMarker("event_id", PTTG_AUDIT_HISTORY_RESPONSE_SUCCESS)) &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("request_duration_ms");
        }));
    }

    @Test
    public void retrieveAuditHistory_nullToDate_useToday() {
        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);

        historyResource.retrieveAuditHistory(null, eventTypes, SOME_PAGEABLE);

        verify(mockHistoryService).getAuditHistory(LocalDate.now(), eventTypes, SOME_PAGEABLE);
    }

    @Test
    public void retrieveAuditHistory_nullToDate_logNull() {
        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);

        historyResource.retrieveAuditHistory(null, eventTypes, SOME_PAGEABLE);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            String expectedLogMessage = String.format("Requested Audit History for events [%s, %s] up to end date %s with pageable of %s",
                    INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE, null, SOME_PAGEABLE);
            return loggingEvent.getFormattedMessage().equals(expectedLogMessage) &&
                    loggingEvent.getArgumentArray()[3].equals(new ObjectAppendingMarker("event_id", PTTG_AUDIT_HISTORY_REQUEST_RECEIVED));
        }));
    }

    @Test
    public void getRecordsForCorrelationId_givenParameters_callAuditHistoryService() {
        String someCorrelationId = "some correlation ID";
        List<AuditEventType> someEventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);

        historyResource.getRecordsForCorrelationId(someCorrelationId, someEventTypes);

        verify(mockHistoryService).getRecordsForCorrelationId(someCorrelationId, someEventTypes);
    }

    @Test
    public void getRecordsForCorrelationId_recordsFromService_returned() {
        List<AuditRecord> expectedRecords = Collections.singletonList(AUDIT_RECORD);
        when(mockHistoryService.getRecordsForCorrelationId(any(), any()))
                .thenReturn(expectedRecords);

        List<AuditEventType> anyEventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
        List<AuditRecord> actualRecords = historyResource.getRecordsForCorrelationId("any correlation ID", anyEventTypes);

        assertThat(actualRecords).isEqualTo(expectedRecords);
    }

    @Test
    public void getRecordsForCorrelationId_givenParameters_logParameters() {
        String someCorrelationId = "some correlation ID";
        List<AuditEventType> someEventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);

        historyResource.getRecordsForCorrelationId(someCorrelationId, someEventTypes);

        String expectedMessage = String.format("Requested audit records for correlationID %s and events [%s, %s]", someCorrelationId,
                INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;
            return loggingEvent.getFormattedMessage().equals(expectedMessage) &&
                    loggingEvent.getArgumentArray()[2].equals(new ObjectAppendingMarker("event_id", PTTG_AUDIT_HISTORY_BY_CORRELATION_ID_REQUEST_RECEIVED));
        }));
    }

    @Test
    public void getRecordsForCorrelationId_returnedRecords_logCount() {
        LocalDateTime anyDate = LocalDateTime.now();
        AuditEventType anyEventType = INCOME_PROVING_FINANCIAL_STATUS_REQUEST;
        Map<String, Object> anyDetail = Collections.emptyMap();

        List<AuditRecord> someAuditRecords = Arrays.asList(new AuditRecord("any id", anyDate, "any email", anyEventType, anyDetail, "any nino"),
                new AuditRecord("any id", anyDate, "any email", anyEventType, anyDetail, "any nino"),
                new AuditRecord("any id", anyDate, "any email", anyEventType, anyDetail, "any nino"));

        given(mockHistoryService.getRecordsForCorrelationId(any(), any()))
                .willReturn(someAuditRecords);

        historyResource.getRecordsForCorrelationId("some-correlation-ID", Collections.singletonList(anyEventType));

        String expectedMessage = "Returned 3 audit records for correlation ID some-correlation-ID";
        then(mockAppender)
                .should()
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;
                    return loggingEvent.getFormattedMessage().equals(expectedMessage) &&
                            argument.getArgumentArray()[2].equals(new ObjectAppendingMarker("event_id", PTTG_AUDIT_HISTORY_BY_CORRELATION_ID_RESPONSE_SUCCESS)) &&
                            ((ObjectAppendingMarker) argument.getArgumentArray()[3]).getFieldName().equals("request_duration_ms");
                }));
    }
    // TODO OJR EE-19133 do IntTest, do WebTest
}
