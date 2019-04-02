package uk.gov.digital.ho.pttg;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.digital.ho.pttg.application.ArchiveException;
import uk.gov.digital.ho.pttg.application.ServiceConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.pttg.AuditEventType.ARCHIVED_RESULTS;
import static uk.gov.digital.ho.pttg.application.LogEvent.PTTG_AUDIT_ARCHIVE_FAILURE;
import static uk.gov.digital.ho.pttg.application.LogEvent.PTTG_AUDIT_CONFIG_MISMATCH;

@RunWith(MockitoJUnitRunner.class)
public class ArchiveServiceTest {

    @Mock private AuditEntryJpaRepository mockRepository;
    @Mock private Appender<ILoggingEvent> mockAppender;

    private ArchiveService archiveService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Captor private ArgumentCaptor<AuditEntry> captorAuditEntry;
    @Captor private ArgumentCaptor<ILoggingEvent> loggingEventArgumentCaptor;

    @Before
    public void setup() {
        ServiceConfiguration serviceConfiguration = new ServiceConfiguration(objectMapper, 0, 0);
        objectMapper = ReflectionTestUtils.invokeMethod(serviceConfiguration, "initialiseObjectMapper", objectMapper);
        archiveService = new ArchiveService(mockRepository, objectMapper);
        Logger logger = (Logger) LoggerFactory.getLogger(ArchiveService.class);
        logger.addAppender(mockAppender);
    }

    @Test
    public void createArchivedResult() {
        String expectedDetail = "{\"results\": {\"PASS\": 1}}";

        AuditEntry archivedResult = archiveService.createArchivedResult(LocalDate.now(), "PASS");

        assertThat(archivedResult.getTimestamp().toLocalDate()).isEqualTo(LocalDate.now());
        assertThat(archivedResult.getType()).isEqualTo(ARCHIVED_RESULTS);
        JSONAssert.assertEquals(expectedDetail, archivedResult.getDetail(), false);
    }

    @Test
    public void incrementArchivedResult_ok() {
        AuditEntry existingResult = auditEntry(LocalDate.now(), "{\"results\": {\"PASS\": 1, \"FAIL\": 1}}");

        String expectedDetail = "{\"results\": {\"PASS\": 2, \"FAIL\": 1}}";

        AuditEntry archivedResult = archiveService.incrementArchivedResult(existingResult, "PASS");

        assertThat(archivedResult.getTimestamp().toLocalDate()).isEqualTo(LocalDate.now());
        assertThat(archivedResult.getType()).isEqualTo(ARCHIVED_RESULTS);
        JSONAssert.assertEquals(expectedDetail, archivedResult.getDetail(), false);
    }

    @Test(expected = ArchiveException.class)
    public void incrementArchivedResult_malformedExistingResult_throwsException() {
        AuditEntry existingResult = auditEntry(LocalDate.now(), "{\"this_should_never_happen\": 1");

        archiveService.incrementArchivedResult(existingResult, "PASS");
    }

    @Test
    public void incrementArchivedResult_malformedExistingResult_logsError() {
        AuditEntry existingResult = auditEntry(LocalDate.now(), "{\"this_should_never_happen\": 1");

        try {
            archiveService.incrementArchivedResult(existingResult, "PASS");
        } catch(Exception e) {
            // not testing the exception handling here
        }

        verify(mockAppender).doAppend(loggingEventArgumentCaptor.capture());
        ILoggingEvent loggingEvent = loggingEventArgumentCaptor.getValue();
        assertThat((loggingEvent.getArgumentArray()[1]))
                .isEqualTo(new ObjectAppendingMarker("event_id", PTTG_AUDIT_ARCHIVE_FAILURE));
    }

    @Test(expected = ArchiveException.class)
    public void incrementArchivedResult_wrongKeyForExistingResult_throwsException() {
        AuditEntry existingResult = auditEntry(LocalDate.now(), "{\"wrong_key\": {\"PASS\": 1, \"FAIL\": 1}}");

        archiveService.incrementArchivedResult(existingResult, "PASS");
    }

    @Test
    public void incrementArchivedResult_wrongKeyForExistingResult_logsError() {
        AuditEntry existingResult = auditEntry(LocalDate.now(), "{\"wrong_key\": {\"PASS\": 1, \"FAIL\": 1}}");

        try {
            archiveService.incrementArchivedResult(existingResult, "PASS");
        } catch(Exception e) {
            // not testing the exception handling here
        }

        verify(mockAppender).doAppend(loggingEventArgumentCaptor.capture());
        ILoggingEvent loggingEvent = loggingEventArgumentCaptor.getValue();
        assertThat((loggingEvent.getArgumentArray()[0]))
                .isEqualTo(new ObjectAppendingMarker("event_id", PTTG_AUDIT_ARCHIVE_FAILURE));
    }

    @Test
    public void archiveResult_today_getsFullDay() {
        LocalDateTime expectedStart = LocalDate.now().atStartOfDay();
        LocalDateTime expectedEnd = LocalDate.now().plusDays(1).atStartOfDay();
        when(mockRepository.findArchivedResults(expectedStart, expectedEnd)).thenReturn(Collections.emptyList());

        archiveService.archiveResult(LocalDate.now(), "PASS");

        verify(mockRepository).save(captorAuditEntry.capture());

        AuditEntry actualAuditEntry = captorAuditEntry.getValue();
        assertThat(actualAuditEntry.getTimestamp().toLocalDate()).isEqualTo(LocalDate.now());
    }

    @Test
    public void archiveResult_newResult_resultSaved() {
        when(mockRepository.findArchivedResults(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        archiveService.archiveResult(LocalDate.now(), "PASS");

        verify(mockRepository).save(captorAuditEntry.capture());

        AuditEntry actualAuditEntry = captorAuditEntry.getValue();
        JSONAssert.assertEquals("{\"results\": {\"PASS\": 1}}", actualAuditEntry.getDetail(), false);
    }

    @Test
    public void archiveResult_existingArchiveNewResult_resultSaved() {
        AuditEntry existingArchive = auditEntry(LocalDate.now(), "{\"results\": { \"FAIL\": 1}}");
        when(mockRepository.findArchivedResults(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(Collections.singletonList(existingArchive));

        archiveService.archiveResult(LocalDate.now(), "PASS");

        verify(mockRepository).save(captorAuditEntry.capture());

        AuditEntry actualAuditEntry = captorAuditEntry.getValue();
        JSONAssert.assertEquals("{\"results\": {\"PASS\": 1, \"FAIL\": 1}}", actualAuditEntry.getDetail(), false);
    }

    @Test
    public void archiveResult_existingArchiveExistingResult_resultAppended() {
        AuditEntry existingArchive = auditEntry(LocalDate.now(), "{\"results\": { \"PASS\": 1}}");
        when(mockRepository.findArchivedResults(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(Collections.singletonList(existingArchive));

        archiveService.archiveResult(LocalDate.now(), "PASS");

        verify(mockRepository).save(captorAuditEntry.capture());

        AuditEntry actualAuditEntry = captorAuditEntry.getValue();
        JSONAssert.assertEquals("{\"results\": {\"PASS\": 2}}", actualAuditEntry.getDetail(), false);
    }

    @Test(expected = ArchiveException.class)
    public void archiveResult_multipleArchivesFoundForDate_exceptionThrown() {
        AuditEntry existingArchive = auditEntry(LocalDate.now(), "{\"results\": { \"PASS\": 1}}");
        when(mockRepository.findArchivedResults(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(Arrays.asList(existingArchive, existingArchive));

        archiveService.archiveResult(LocalDate.now(), "PASS");
    }

    @Test
    public void archiveResult_multipleArchivesFoundForDate_errorLogged() {
        AuditEntry existingArchive = auditEntry(LocalDate.now(), "{\"results\": { \"PASS\": 1}}");
        when(mockRepository.findArchivedResults(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(Arrays.asList(existingArchive, existingArchive));

        try {
            archiveService.archiveResult(LocalDate.now(), "PASS");
        } catch(Exception e) {
            // not testing the exception handling here
        }

        verify(mockAppender).doAppend(loggingEventArgumentCaptor.capture());
        ILoggingEvent loggingEvent = loggingEventArgumentCaptor.getValue();
        assertThat((loggingEvent.getArgumentArray()[0]))
                .isEqualTo(new ObjectAppendingMarker("event_id", PTTG_AUDIT_ARCHIVE_FAILURE));
    }

    private AuditEntry auditEntry(LocalDate date, String detail) {
        return new AuditEntry(
                UUID.randomUUID().toString(),
                date.atStartOfDay(),
                "",
                UUID.randomUUID().toString(),
                "Audit Service",
                "",
                "",
                ARCHIVED_RESULTS,
                detail
        );
    }
}
