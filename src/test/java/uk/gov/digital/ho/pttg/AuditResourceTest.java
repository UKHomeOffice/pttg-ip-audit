package uk.gov.digital.ho.pttg;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.collect.ImmutableMap;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import uk.gov.digital.ho.pttg.api.AuditRecord;
import uk.gov.digital.ho.pttg.api.AuditResource;
import uk.gov.digital.ho.pttg.api.AuditableData;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditResourceTest {

    private static final AuditRecord AUDIT_RECORD = new AuditRecord("some id",
            LocalDateTime.of(2017, 12, 8, 0, 0),
            "some email",
            AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_REQUEST,
            ImmutableMap.of("some key", "some value"),
            "some nino"
    );

    @Mock private Appender<ILoggingEvent> mockAppender;
    @Mock private AuditService mockService;

    private AuditResource resource;

    @Before
    public void setUp() {
        resource = new AuditResource(mockService);
        Logger rootLogger = (Logger) LoggerFactory.getLogger(AuditResource.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);
    }

    @Test
    public void shouldUseCollaboratorsForRetrieveAllAuditData() {

        when(mockService.getAllAuditData(any(Pageable.class))).thenReturn(Collections.singletonList(AUDIT_RECORD));

        Pageable somePageable = mock(Pageable.class);

        resource.retrieveAllAuditData(somePageable);

        verify(mockService).getAllAuditData(somePageable);
    }

    @Test
    public void shouldUsePaginationObjectForRetrieveAllAuditData() {

        Pageable pageable = mock(Pageable.class);

        when(mockService.getAllAuditData(pageable)).thenReturn(Collections.singletonList(AUDIT_RECORD));

        resource.retrieveAllAuditData(pageable);

        verify(mockService).getAllAuditData(pageable);
    }

    @Test
    public void shouldUseReturnAuditRecords() {

        when(mockService.getAllAuditData(null)).thenReturn(Collections.singletonList(AUDIT_RECORD));

        List<AuditRecord> auditRecords = resource.retrieveAllAuditData(null);

        assertThat(auditRecords).containsExactly(AUDIT_RECORD);
    }

    @Test
    public void shouldUseCollaboratorsForRecordAuditEntry() {

        AuditableData auditableData = new AuditableData("some event id",
                LocalDateTime.of(2017,12, 8, 0, 0),
                "some session id",
                "some correlation id",
                "some user id",
                "some deployment name",
                "some deployment namespace",
                AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
                "{}");

        resource.recordAuditEntry(auditableData);

        verify(mockService).add(auditableData);
    }

    @Test
    public void shouldLogRecordAuditEntry() {
        AuditableData auditableData = new AuditableData("some event id",
                LocalDateTime.of(2017,12, 8, 0, 0),
                "some session id",
                "some correlation id",
                "some user id",
                "some deployment name",
                "some deployment namespace",
                AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
                "{}");

        resource.recordAuditEntry(auditableData);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Audited INCOME_PROVING_FINANCIAL_STATUS_RESPONSE for correlation id some correlation id") &&
                    (loggingEvent.getArgumentArray()[1]).equals("some correlation id") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[3]).getFieldName().equals("request_duration_ms");
        }));
    }

    @Test
    public void shouldLogWhenGetAccessCodeRequestReceived() {
        when(mockService.getAllAuditData(null)).thenReturn(Collections.singletonList(AUDIT_RECORD));
        resource.retrieveAllAuditData(null);
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;
            return loggingEvent.getFormattedMessage().startsWith("Audit records requested") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogGetAccessCodeResponseSuccess() {
        when(mockService.getAllAuditData(null)).thenReturn(Collections.singletonList(AUDIT_RECORD));
        resource.retrieveAllAuditData(null);
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("1 audit records found") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("request_duration_ms");
        }));
    }
}