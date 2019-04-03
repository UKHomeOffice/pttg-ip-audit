package uk.gov.digital.ho.pttg.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import uk.gov.digital.ho.pttg.ArchiveService;

import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@RunWith(MockitoJUnitRunner.class)
public class ArchiveResourceTest {

    @Mock private Appender<ILoggingEvent> mockAppender;
    @Mock private ArchiveService mockArchiveService;
    @Mock private RequestData mockRequestData;

    private ArchiveResource archiveResource;

    private static final String ANY_RESULT = "any-result";
    private static final LocalDate ANY_LAST_ARCHIVE_DATE = LocalDate.now();
    private static final LocalDate ANY_RESULT_DATE = LocalDate.now().minusDays(1);
    private static final String ANY_NINO = "any-nino";
    private static final List<String> ANY_EVENT_IDS = asList("any-event-id1", "any-event-id2");
    private static final ArchiveRequest ANY_ARCHIVE_REQUEST = new ArchiveRequest(ANY_RESULT, ANY_LAST_ARCHIVE_DATE, ANY_EVENT_IDS, ANY_NINO);

    @Before
    public void setUp() {
        archiveResource = new ArchiveResource(mockArchiveService, mockRequestData);
        Logger rootLogger = (Logger) LoggerFactory.getLogger(ArchiveResource.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);
    }

    @Test
    public void archiveResult_callArchiveService() {
        archiveResource.archiveResult(ANY_RESULT_DATE, ANY_ARCHIVE_REQUEST);

        verify(mockArchiveService).archiveResult(ANY_RESULT_DATE, ANY_ARCHIVE_REQUEST.result(), ANY_ARCHIVE_REQUEST.eventIds(), ANY_ARCHIVE_REQUEST.lastArchiveDate());
    }

    @Test
    public void archiveResult_logsRequestParameters() {
        archiveResource.archiveResult(ANY_RESULT_DATE, ANY_ARCHIVE_REQUEST);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            String expectedLogMessage = String.format("Requested archiveResult for date %s with details %s",
                    ANY_RESULT_DATE,
                    ANY_ARCHIVE_REQUEST);
            return loggingEvent.getFormattedMessage().equals(expectedLogMessage) &&
                    loggingEvent.getArgumentArray()[2].equals(new ObjectAppendingMarker("event_id", PTTG_AUDIT_ARCHIVE_RESULT_REQUEST_RECEIVED));
        }));
    }

    @Test
    public void archiveResult_logsSuccess() {
        archiveResource.archiveResult(ANY_RESULT_DATE, ANY_ARCHIVE_REQUEST);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            String expectedLogMessage = "ArchiveResult request completed successfully";
            return loggingEvent.getFormattedMessage().equals(expectedLogMessage) &&
                    loggingEvent.getArgumentArray()[0].equals(new ObjectAppendingMarker("event_id", PTTG_AUDIT_ARCHIVE_RESULT_RESPONSE_SUCCESS)) &&
                    ((ObjectAppendingMarker)loggingEvent.getArgumentArray()[1]).getFieldName().equals("request_duration_ms");
        }));
    }



}
