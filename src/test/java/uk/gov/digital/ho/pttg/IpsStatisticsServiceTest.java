package uk.gov.digital.ho.pttg;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.digital.ho.pttg.api.IpsStatistics;
import uk.gov.digital.ho.pttg.application.IpsStatisticsException;
import uk.gov.digital.ho.pttg.application.ServiceConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.pttg.IpsStatisticsService.NO_STATISTICS;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@RunWith(MockitoJUnitRunner.class)
public class IpsStatisticsServiceTest {

    private static final LocalDate ANY_DATE = LocalDate.now();
    private static final int ANY_INT = 0;
    private static final String ANY_STRING = "any";
    private static final LocalDateTime ANY_DATE_TIME = LocalDateTime.now();
    private static final IpsStatistics ANY_IPS_STATS = statsFor(ANY_DATE, ANY_DATE);

    @Mock
    private AuditEntryJpaRepository mockRepository;
    @Mock
    private Appender<ILoggingEvent> mockAppender;
    private ArgumentCaptor<LoggingEvent> logCaptor;

    private ArgumentCaptor<AuditEntry> auditEntryCaptor;

    private ObjectMapper objectMapper = new ObjectMapper();
    private IpsStatisticsService service;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        ServiceConfiguration serviceConfiguration = new ServiceConfiguration(objectMapper, 0, 0);
        objectMapper = ReflectionTestUtils.invokeMethod(serviceConfiguration, "initialiseObjectMapper", objectMapper);
        service = new IpsStatisticsService(mockRepository, objectMapper);

        Logger logger = (Logger) LoggerFactory.getLogger(IpsStatisticsService.class);
        logger.addAppender(mockAppender);

        logCaptor = ArgumentCaptor.forClass(LoggingEvent.class);
        auditEntryCaptor = ArgumentCaptor.forClass(AuditEntry.class);
    }

    @Test
    public void getIpsStatistics_noStatisticsInDatabase_returnNO_STATISTICS() {
        given(mockRepository.findAllIpsStatistics()).willReturn(emptyList());

        IpsStatistics actualStatistics = service.getIpsStatistics(ANY_DATE, ANY_DATE);
        assertThat(actualStatistics).isEqualTo(NO_STATISTICS);
    }

    @Test
    public void getIpsStatistics_noStatisticsForDates_returnNO_STATISTICS() {
        IpsStatistics notRequestedStatistics = statsFor("2019-03-01", "2019-03-31");
        List<AuditEntry> auditEntries = singletonList(entryFor(notRequestedStatistics));

        given(mockRepository.findAllIpsStatistics()).willReturn(auditEntries);

        IpsStatistics actualStatistics = service.getIpsStatistics(LocalDate.parse("2019-04-01"), LocalDate.parse("2019-04-30"));
        assertThat(actualStatistics).isEqualTo(NO_STATISTICS);
    }

    @Test
    public void getIpsStatistics_statisticsForDate_returnStatistics() {
        LocalDate someFromDate = LocalDate.parse("2019-05-01");
        LocalDate someToDate = LocalDate.parse("2019-05-31");

        IpsStatistics expectedStatistics = statsFor(someFromDate, someToDate);
        List<AuditEntry> auditEntries = singletonList(entryFor(expectedStatistics));

        given(mockRepository.findAllIpsStatistics()).willReturn(auditEntries);

        IpsStatistics actualStatistics = service.getIpsStatistics(someFromDate, someToDate);
        assertThat(actualStatistics).isEqualTo(expectedStatistics);
    }

    @Test
    public void getIpsStatistics_sameFromDate_differentToDate_returnNO_STATISTICS() {
        LocalDate someFromDate = LocalDate.parse("2018-03-01");
        IpsStatistics sameFromDateStatistics = statsFor(someFromDate, LocalDate.parse("2018-03-29"));
        List<AuditEntry> auditEntries = singletonList(entryFor(sameFromDateStatistics));

        given(mockRepository.findAllIpsStatistics()).willReturn(auditEntries);

        IpsStatistics actualStatistics = service.getIpsStatistics(someFromDate, LocalDate.parse("2019-02-28"));
        assertThat(actualStatistics).isEqualTo(NO_STATISTICS);
    }

    @Test
    public void getIpsStatistics_sameToDate_differentFromDate_returnNO_STATISTICS() {
        LocalDate someToDate = LocalDate.parse("2019-03-29");
        IpsStatistics sameToDateStatistics = statsFor(LocalDate.parse("2019-03-01"), someToDate);
        List<AuditEntry> auditEntries = singletonList(entryFor(sameToDateStatistics));

        given(mockRepository.findAllIpsStatistics()).willReturn(auditEntries);

        IpsStatistics actualStatistics = service.getIpsStatistics(LocalDate.parse("2018-04-05"), someToDate);
        assertThat(actualStatistics).isEqualTo(NO_STATISTICS);
    }

    @Test
    public void getIpsStatistics_multipleResults_returnRequested() {
        LocalDate someFromDate = LocalDate.parse("2019-08-01");
        LocalDate someToDate = LocalDate.parse("2019-07-31");
        IpsStatistics expectedStatistics = statsFor(someFromDate, someToDate);

        List<AuditEntry> auditEntries = asList(
                entryFor(ANY_IPS_STATS),
                entryFor(expectedStatistics),
                entryFor(ANY_IPS_STATS));

        given(mockRepository.findAllIpsStatistics()).willReturn(auditEntries);

        IpsStatistics actualStatistics = service.getIpsStatistics(someFromDate, someToDate);
        assertThat(actualStatistics).isEqualTo(expectedStatistics);
    }

    @Test
    public void getIpsStatistics_malformedEntry_logError() {
        String malformedStatistics = "malformed!";
        AuditEntry malformedEntry = new AuditEntry(ANY_STRING, ANY_DATE_TIME, ANY_STRING, ANY_STRING, ANY_STRING, ANY_STRING, ANY_STRING,
                                                   AuditEventType.IPS_STATISTICS, malformedStatistics);

        given(mockRepository.findAllIpsStatistics()).willReturn(singletonList(malformedEntry));

        try {
            service.getIpsStatistics(ANY_DATE, ANY_DATE);
        } catch (IpsStatisticsException ignored) {
            // Not of interest to this test
        }

        then(mockAppender).should().doAppend(logCaptor.capture());
        assertErrorLog(logCaptor.getValue(), "Malformed", malformedStatistics);
    }

    @Test
    public void getIpsStatistics_malformedEntry_throwIpsStatisticsException() {
        expectedException.expect(IpsStatisticsException.class);
        expectedException.expectMessage(startsWith("Malformed"));

        String malformedStatistics = "malformed!";
        AuditEntry malformedEntry = new AuditEntry(ANY_STRING, ANY_DATE_TIME, ANY_STRING, ANY_STRING, ANY_STRING, ANY_STRING, ANY_STRING,
                                                   AuditEventType.IPS_STATISTICS, malformedStatistics);

        given(mockRepository.findAllIpsStatistics()).willReturn(singletonList(malformedEntry));

        service.getIpsStatistics(ANY_DATE, ANY_DATE);
    }

    @Test
    public void getIpsStatistics_multipleResults_logError() {
        LocalDate someFromDate = LocalDate.parse("2019-08-01");
        LocalDate someToDate = LocalDate.parse("2019-07-31");
        IpsStatistics duplicatedStatistics = statsFor(someFromDate, someToDate);

        given(mockRepository.findAllIpsStatistics()).willReturn(asList(entryFor(duplicatedStatistics),
                                                                       entryFor(duplicatedStatistics)));

        try {
            service.getIpsStatistics(someFromDate, someToDate);
        } catch (IpsStatisticsException ignored) {
            // Not of interest to this test
        }

        then(mockAppender).should().doAppend(logCaptor.capture());
        assertErrorLog(logCaptor.getValue(), "Multiple", someFromDate.toString(), someToDate.toString());
    }

    @Test
    public void getIpsStatistics_multipleResults_throwIpsStatisticsException() {
        expectedException.expect(IpsStatisticsException.class);
        expectedException.expectMessage(startsWith("Multiple"));

        LocalDate someFromDate = LocalDate.parse("2019-08-01");
        LocalDate someToDate = LocalDate.parse("2019-07-31");
        IpsStatistics duplicatedStatistics = statsFor(someFromDate, someToDate);

        given(mockRepository.findAllIpsStatistics()).willReturn(asList(entryFor(duplicatedStatistics),
                                                                       entryFor(duplicatedStatistics)));

        service.getIpsStatistics(someFromDate, someToDate);
    }

    @Test
    public void storeIpsStatistics_givenStatistics_saveAsAuditEntry() {
        IpsStatistics someStatistics = statsFor(ANY_DATE, ANY_DATE);

        service.storeIpsStatistics(someStatistics);

        then(mockRepository).should().save(auditEntryCaptor.capture());
        AuditEntry savedAuditEntry = auditEntryCaptor.getValue();

        JSONAssert.assertEquals(toJson(someStatistics), savedAuditEntry.getDetail(), false);
        assertThat(savedAuditEntry.getType()).isEqualTo(AuditEventType.IPS_STATISTICS);
    }

    @Test
    public void storeIpsStatistics_alreadyHaveStatisticsForDate_logError() {
        LocalDate someFromDate = LocalDate.parse("2019-06-01");
        LocalDate someToDate = LocalDate.parse("2019-06-30");
        IpsStatistics statistics = statsFor(someFromDate, someToDate);

        List<AuditEntry> alreadyPresentStatistics = singletonList(entryFor(statistics));
        given(mockRepository.findAllIpsStatistics()).willReturn(alreadyPresentStatistics);

        try {
            service.storeIpsStatistics(statistics);
        } catch (IpsStatisticsException ignored) {
            // Not of interest to this test.
        }

        then(mockAppender).should().doAppend(logCaptor.capture());
        assertErrorLog(logCaptor.getValue(), "Statistics already exist", someFromDate.toString(), someToDate.toString());
    }

    @Test
    public void storeIpsStatistics_alreadyHaveStatisticsForDate_throwIpsStatisticsException() {
        expectedException.expect(IpsStatisticsException.class);
        expectedException.expectMessage(containsString("already exist"));

        LocalDate someFromDate = LocalDate.parse("2019-06-01");
        LocalDate someToDate = LocalDate.parse("2019-06-30");
        IpsStatistics statistics = statsFor(someFromDate, someToDate);

        List<AuditEntry> alreadyPresentStatistics = singletonList(entryFor(statistics));
        given(mockRepository.findAllIpsStatistics()).willReturn(alreadyPresentStatistics);

        service.storeIpsStatistics(statistics);
    }

    @Test
    public void storeIpsStatistics_jsonException_logError() throws JsonProcessingException {
        ObjectMapper exceptionThrowingMapper = Mockito.mock(ObjectMapper.class);
        when(exceptionThrowingMapper.writeValueAsString(any(IpsStatistics.class))).thenThrow(new JsonParseException(null, "JSON error"));

        IpsStatisticsService service = new IpsStatisticsService(mockRepository, exceptionThrowingMapper);
        try {
            service.storeIpsStatistics(ANY_IPS_STATS);
        } catch (IpsStatisticsException ignored) {
            // Not of interest to this test.
        }

        then(mockAppender).should().doAppend(logCaptor.capture());
        assertErrorLog(logCaptor.getValue(), "JSON", "parse", "error");
    }

    @Test
    public void storeIpsStatistics_jsonException_throwIpsStatisticsException() throws JsonProcessingException {
        expectedException.expect(IpsStatisticsException.class);
        expectedException.expectMessage(containsString("parse error"));

        ObjectMapper exceptionThrowingMapper = Mockito.mock(ObjectMapper.class);
        when(exceptionThrowingMapper.writeValueAsString(any(IpsStatistics.class))).thenThrow(new JsonParseException(null, "JSON error"));

        IpsStatisticsService service = new IpsStatisticsService(mockRepository, exceptionThrowingMapper);
        service.storeIpsStatistics(ANY_IPS_STATS);
    }

    private void assertErrorLog(LoggingEvent loggingEvent, String... contents) {
        assertThat(loggingEvent.getFormattedMessage()).contains(contents);
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.ERROR);
        assertThat(loggingEvent.getArgumentArray()).contains(new ObjectAppendingMarker("event_id", PTTG_AUDIT_IPS_STATS_ERROR));
    }

    private static IpsStatistics statsFor(String fromDate, String toDate) {
        return statsFor(LocalDate.parse(fromDate), LocalDate.parse(toDate));
    }

    private static IpsStatistics statsFor(LocalDate fromDate, LocalDate toDate) {
        return new IpsStatistics(fromDate, toDate, ANY_INT, ANY_INT, ANY_INT, ANY_INT);
    }

    private AuditEntry entryFor(IpsStatistics notRequestedStatistics) {
        return new AuditEntry(ANY_STRING, ANY_DATE_TIME, ANY_STRING, ANY_STRING, ANY_STRING, ANY_STRING, ANY_STRING, AuditEventType.IPS_STATISTICS, toJson(notRequestedStatistics));
    }

    private String toJson(IpsStatistics statistics) {
        return "{" +
                String.format("\"From Date\": \"%s\",", statistics.fromDate()) +
                String.format("\"To Date\": \"%s\",", statistics.toDate()) +
                String.format("\"Passed\": %s,", statistics.passed()) +
                String.format("\"Not Passed\": %s,", statistics.notPassed()) +
                String.format("\"Not Found\": %s,", statistics.notFound()) +
                String.format("\"Error\": %s", statistics.error()) +
                "}";
    }
}