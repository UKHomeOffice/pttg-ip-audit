package uk.gov.digital.ho.pttg;

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
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.pttg.application.AuditException;
import uk.gov.digital.ho.pttg.application.ResourceExceptionHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
@RunWith(MockitoJUnitRunner.class)
public class ResourceExceptionHandlerTest {

    private ResourceExceptionHandler resourceExceptionHandler;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Before
    public void setup() {
        resourceExceptionHandler = new ResourceExceptionHandler();
        Logger rootLogger = (Logger) LoggerFactory.getLogger(ResourceExceptionHandler.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);
    }

    @Test
    public void shouldHandleAuditException() {

        AuditException exception = new AuditException("some message");

        ResponseEntity responseEntity = resourceExceptionHandler.handle(exception);

        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(responseEntity.getHeaders()).containsKeys(CONTENT_TYPE);
        assertThat(responseEntity.getHeaders().get(CONTENT_TYPE).size()).isEqualTo(1);
        assertThat(responseEntity.getHeaders().get(CONTENT_TYPE).get(0)).isEqualTo(APPLICATION_JSON_VALUE);
        assertThat(responseEntity.getBody()).isEqualTo("some message");
    }

    @Test
    public void shouldLogErrorForException() {
        AuditException mockException = mock(AuditException.class);
        when(mockException.getMessage()).thenReturn("any message");

        resourceExceptionHandler.handle(mockException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("AuditException: any message") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }
}