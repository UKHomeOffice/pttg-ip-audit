package uk.gov.digital.ho.pttg.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.gov.digital.ho.pttg.AuditEventType;
import uk.gov.digital.ho.pttg.AuditHistoryService;
import uk.gov.digital.ho.pttg.application.LogEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_REQUEST;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditHistoryResourceTest {

    @Mock private Appender<ILoggingEvent> mockAppender;
    @Mock private AuditHistoryService mockHistoryService;
    @Mock private RequestData mockRequestData;

    private AuditHistoryResource historyResource;

    private ArgumentCaptor<LoggingEvent> logCaptor;

    private static final AuditRecord AUDIT_RECORD = new AuditRecord("some id",
            LocalDateTime.of(2017, 12, 8, 0, 0),
            "some email",
            AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_REQUEST,
            ImmutableMap.of("some key", "some value"),
            "some nino"
    );
    private static final Pageable SOME_PAGEABLE = PageRequest.of(5, 8);
    private static final List<AuditEventType> ANY_EVENT_TYPES = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);

    @Before
    public void setUp() {
        historyResource = new AuditHistoryResource(mockHistoryService, mockRequestData);
        Logger rootLogger = (Logger) LoggerFactory.getLogger(AuditHistoryResource.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);

        logCaptor = ArgumentCaptor.forClass(LoggingEvent.class);
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

        then(mockAppender).should(atLeastOnce()).doAppend(logCaptor.capture());

        String expectedLogMessage = String.format("Requested Audit History for events [%s, %s] up to end date %s with pageable of %s",
                    INCOME_PROVING_FINANCIAL_STATUS_REQUEST.name(),
                    INCOME_PROVING_FINANCIAL_STATUS_RESPONSE.name(),
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyy-MM-dd")),
                    SOME_PAGEABLE);

        assertThat(logForEvent(PTTG_AUDIT_HISTORY_REQUEST_RECEIVED).getFormattedMessage())
                .isEqualTo(expectedLogMessage);
    }

    @Test
    public void retrieveAuditHistory_logsResult() {
        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
        when(mockHistoryService.getAuditHistory(LocalDate.now(), eventTypes, SOME_PAGEABLE)).thenReturn(Arrays.asList(AUDIT_RECORD));

        historyResource.retrieveAuditHistory(LocalDate.now(), eventTypes, SOME_PAGEABLE);

        then(mockAppender).should(atLeastOnce()).doAppend(logCaptor.capture());

        LoggingEvent loggingEvent = logForEvent(PTTG_AUDIT_HISTORY_RESPONSE_SUCCESS);
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo("Returned 1 audit record(s) for history request");
        assertRequestDurationLogged(loggingEvent);
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
        then(mockAppender).should(atLeastOnce()).doAppend(logCaptor.capture());

        String expectedLogMessage = String.format("Requested Audit History for events [%s, %s] up to end date %s with pageable of %s",
                                                  INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE, null, SOME_PAGEABLE);
        LoggingEvent loggingEvent = logForEvent(PTTG_AUDIT_HISTORY_REQUEST_RECEIVED);
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo(expectedLogMessage);
    }

    @Test
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void getAllCorrelationIds_givenEventTypes_passToService() {
        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);

        historyResource.getAllCorrelationIds(eventTypes, null);

        verify(mockHistoryService).getAllCorrelationIds(eq(eventTypes));
    }

    @Test
    public void getAllCorrelationIds_correlationIdsFromService_returned() {
        List<String> correlationIds = Arrays.asList("some correlation id", "some other correlation id");
        when(mockHistoryService.getAllCorrelationIds(any()))
                .thenReturn(correlationIds);

        List<String> returnedCorrelationIds = historyResource.getAllCorrelationIds(ANY_EVENT_TYPES, null);

        assertThat(returnedCorrelationIds).isEqualTo(correlationIds);
    }

    @Test
    public void getAllCorrelationIds_givenEventTypes_logEntryParameters() {
        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);

        historyResource.getAllCorrelationIds(eventTypes, null);

        String expectedLogMessage = String.format("Requested all correlation ids for events [%s, %s]",
                INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);

        then(mockAppender).should(atLeastOnce()).doAppend(logCaptor.capture());

        LoggingEvent loggingEvent = logForEvent(PTTG_AUDIT_HISTORY_CORRELATION_IDS_REQUEST_RECEIVED);
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo(expectedLogMessage);
    }

    @Test
    public void getAllCorrelationIds_returnedCorrelationIds_logCount() {
        List<String> correlationIds = Arrays.asList("some correlation id", "some other correlation id", "yet some other correlation id");
        given(mockHistoryService.getAllCorrelationIds(any())).willReturn(correlationIds);

        historyResource.getAllCorrelationIds(ANY_EVENT_TYPES, null);

        then(mockAppender).should(atLeastOnce()).doAppend(logCaptor.capture());

        String expectedMessage = "Returning 3 correlation IDs for all correlation ID request";
        LoggingEvent loggingEvent = logForEvent(PTTG_AUDIT_HISTORY_CORRELATION_IDS_RESPONSE_SUCCESS);
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo(expectedMessage);
        assertRequestDurationLogged(loggingEvent);
    }

    @Test
    public void getAllCorrelationIds_withToDate_callService() {
        List<AuditEventType> someEventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
        LocalDate someDate = LocalDate.now();

        historyResource.getAllCorrelationIds(someEventTypes, someDate);

        then(mockHistoryService).should().getAllCorrelationIds(someEventTypes, someDate);
    }

    @Test
    public void getAllCorrelationIds_withToDate_returnCorrelationIdsFromService() {
        List<String> expectedCorrelationIds = Arrays.asList("some correlation id", "some other correlation id");
        given(mockHistoryService.getAllCorrelationIds(anyList(), any(LocalDate.class)))
                .willReturn(expectedCorrelationIds);

        List<AuditEventType> anyEventTypes = Collections.singletonList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST);
        LocalDate anyDate = LocalDate.now();
        List<String> actualCorrelationIds = historyResource.getAllCorrelationIds(anyEventTypes, anyDate);

        assertThat(actualCorrelationIds).isEqualTo(expectedCorrelationIds);
    }

    @Test
    public void getAllCorrelationIds_withToDate_logEntryParameters() {
        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
        LocalDate toDate = LocalDate.parse("2019-07-30");

        historyResource.getAllCorrelationIds(eventTypes, toDate);

        then(mockAppender).should(atLeastOnce()).doAppend(logCaptor.capture());

        String expectedLogMessage = String.format("Requested all correlation ids for events [%s, %s] up to %s",
                                                  INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE, "2019-07-30");
        LoggingEvent loggingEvent = logForEvent(PTTG_AUDIT_HISTORY_CORRELATION_IDS_REQUEST_RECEIVED);
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo(expectedLogMessage);
    }

    @Test
    public void getAllCorrelationIds_withToDate_idsReturned_logCount() {
        List<String> correlationIds = Arrays.asList("some correlation id", "some other correlation id");
        given(mockHistoryService.getAllCorrelationIds(anyList(), any(LocalDate.class))).willReturn(correlationIds);

        LocalDate anyDate = LocalDate.now();
        historyResource.getAllCorrelationIds(ANY_EVENT_TYPES, anyDate);

        then(mockAppender).should(atLeastOnce()).doAppend(logCaptor.capture());

        String expectedMessage = "Returning 2 correlation IDs for all correlation ID request";
        LoggingEvent loggingEvent = logForEvent(PTTG_AUDIT_HISTORY_CORRELATION_IDS_RESPONSE_SUCCESS);
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo(expectedMessage);
        assertRequestDurationLogged(loggingEvent);
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

        List<AuditRecord> actualRecords = historyResource.getRecordsForCorrelationId("any correlation ID", ANY_EVENT_TYPES);

        assertThat(actualRecords).isEqualTo(expectedRecords);
    }

    @Test
    public void getRecordsForCorrelationId_givenParameters_logParameters() {
        String someCorrelationId = "some correlation ID";
        List<AuditEventType> someEventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);

        historyResource.getRecordsForCorrelationId(someCorrelationId, someEventTypes);

        then(mockAppender).should(atLeastOnce()).doAppend(logCaptor.capture());

        String expectedMessage = String.format("Requested audit records for correlationID %s and events [%s, %s]", someCorrelationId,
                                               INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);

        LoggingEvent loggingEvent = logForEvent(PTTG_AUDIT_HISTORY_BY_CORRELATION_ID_REQUEST_RECEIVED);
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo(expectedMessage);
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

        then(mockAppender).should(atLeastOnce()).doAppend(logCaptor.capture());

        String expectedMessage = "Returned 3 audit records for correlation ID some-correlation-ID";
        LoggingEvent loggingEvent = logForEvent(PTTG_AUDIT_HISTORY_BY_CORRELATION_ID_RESPONSE_SUCCESS);
        assertThat(loggingEvent.getFormattedMessage()).isEqualTo(expectedMessage);
        assertRequestDurationLogged(loggingEvent);
    }

    private LoggingEvent logForEvent(LogEvent logEvent) {
        return logCaptor.getAllValues().stream()
                        .filter(loggingEvent -> ArrayUtils.contains(loggingEvent.getArgumentArray(), new ObjectAppendingMarker("event_id", logEvent)))
                        .findFirst()
                        .orElseThrow(AssertionError::new);
    }

    private void assertRequestDurationLogged(LoggingEvent loggingEvent) {
        boolean hasRequestDuration = Arrays.stream(loggingEvent.getArgumentArray()).anyMatch(AuditHistoryResourceTest::isRequestDuration);
        assertThat(hasRequestDuration).isTrue();
    }

    private static boolean isRequestDuration(Object logArg) {
        return logArg instanceof ObjectAppendingMarker &&
                ((ObjectAppendingMarker) logArg).getFieldName().equals("request_duration_ms");
    }
}
