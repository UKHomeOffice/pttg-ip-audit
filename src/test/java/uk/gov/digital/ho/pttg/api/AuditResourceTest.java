package uk.gov.digital.ho.pttg.api;

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import java.time.LocalDateTime;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import uk.gov.digital.ho.pttg.AuditEventType;
import uk.gov.digital.ho.pttg.AuditService;

@RunWith(MockitoJUnitRunner.class)
public class AuditResourceTest {

    @Mock private Appender<ILoggingEvent> mockAppender;
    @Mock private AuditService mockService;
    @Mock private RequestData mockRequestData;

    private AuditResource resource;

    @Before
    public void setUp() {
        resource = new AuditResource(mockService, mockRequestData);
        Logger rootLogger = (Logger) LoggerFactory.getLogger(AuditResource.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);
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
            return loggingEvent.getFormattedMessage().equals("Audit request INCOME_PROVING_FINANCIAL_STATUS_RESPONSE received for correlation id some correlation id") &&
                    (loggingEvent.getArgumentArray()[1]).equals("some correlation id") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id") ;
        }));

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;
            return loggingEvent.getFormattedMessage().equals("Audit request INCOME_PROVING_FINANCIAL_STATUS_RESPONSE completed for correlation id some correlation id") &&
                    (loggingEvent.getArgumentArray()[1]).equals("some correlation id") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[3]).getFieldName().equals("request_duration_ms");
        }));
    }
}
