package uk.gov.digital.ho.pttg;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.collect.ImmutableMap;
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
import java.util.Arrays;
import java.util.List;

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
    public void retrieveAuditHistory_callsAuditHistoryService() {
        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
        when(mockHistoryService.getAuditHistory(LocalDate.now(), eventTypes)).thenReturn(Arrays.asList(AUDIT_RECORD));

        List<AuditRecord> result = historyResource.retrieveAuditHistory(LocalDate.now(), eventTypes);

        verify(mockHistoryService).getAuditHistory(LocalDate.now(), eventTypes);
    }


}
