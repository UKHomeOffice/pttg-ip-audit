package uk.gov.digital.ho.pttg.api.ipsstatistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.digital.ho.pttg.AuditEntry;
import uk.gov.digital.ho.pttg.AuditEntryJpaRepository;
import uk.gov.digital.ho.pttg.AuditEventType;
import uk.gov.digital.ho.pttg.application.ServiceConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static uk.gov.digital.ho.pttg.api.ipsstatistics.IpsStatisticsService.NO_STATISTICS;

@RunWith(MockitoJUnitRunner.class)
public class IpsStatisticsServiceTest {

    private static final LocalDate ANY_DATE = LocalDate.now();
    private static final int ANY_INT = 0;
    private static final String ANY_STRING = "any";
    private static final LocalDateTime ANY_DATE_TIME = LocalDateTime.now();

    @Mock
    private AuditEntryJpaRepository mockRepository;

    private ObjectMapper objectMapper = new ObjectMapper();
    private IpsStatisticsService service;

    @Before
    public void setUp() {
        ServiceConfiguration serviceConfiguration = new ServiceConfiguration(objectMapper, 0, 0);
        objectMapper = ReflectionTestUtils.invokeMethod(serviceConfiguration, "initialiseObjectMapper", objectMapper);
        service = new IpsStatisticsService(mockRepository, objectMapper);
    }

    @Test
    public void getIpsStatistics_anyDates_fetchStatisticsFromDatabase() {
        service.getIpsStatistics(ANY_DATE, ANY_DATE);

        then(mockRepository).should().findAllIpsStatistics();
    }

    @Test
    public void getIpsStatistics_noStatisticsInDatabase_returnNO_STATISTICS() {
        given(mockRepository.findAllIpsStatistics()).willReturn(emptyList());

        IpsStatistics actualStatistics = service.getIpsStatistics(ANY_DATE, ANY_DATE);
        assertThat(actualStatistics).isEqualTo(NO_STATISTICS);
    }

    @Test
    public void getIpsStatistics_noStatisticsForDates_returnNO_STATISTICS() {
        IpsStatistics notRequestedStatistics = new IpsStatistics(LocalDate.parse("2019-03-01"), LocalDate.parse("2019-03-31"), ANY_INT, ANY_INT, ANY_INT, ANY_INT);
        List<AuditEntry> auditEntries = singletonList(entryFor(notRequestedStatistics));

        given(mockRepository.findAllIpsStatistics()).willReturn(auditEntries);

        IpsStatistics actualStatistics = service.getIpsStatistics(LocalDate.parse("2019-04-01"), LocalDate.parse("2019-04-30"));
        assertThat(actualStatistics).isEqualTo(NO_STATISTICS);
    }

    @Test
    public void getIpsStatistics_statisticsForDate_returnStatistics() {
        LocalDate someFromDate = LocalDate.parse("2019-05-01");
        LocalDate someToDate = LocalDate.parse("2019-05-31");

        IpsStatistics expectedStatistics = new IpsStatistics(someFromDate, someToDate, ANY_INT, ANY_INT, ANY_INT, ANY_INT);
        List<AuditEntry> auditEntries = singletonList(entryFor(expectedStatistics));

        given(mockRepository.findAllIpsStatistics()).willReturn(auditEntries);

        IpsStatistics actualStatistics = service.getIpsStatistics(someFromDate, someToDate);
        assertThat(actualStatistics).isEqualTo(expectedStatistics);
    }

    @Test
    public void getIpsStatistics_sameFromDate_differentToDate_returnNO_STATISTICS() {
        LocalDate someFromDate = LocalDate.parse("2018-03-01");
        IpsStatistics sameFromDateStatistics = new IpsStatistics(someFromDate, LocalDate.parse("2018-03-29"), ANY_INT, ANY_INT, ANY_INT, ANY_INT);
        List<AuditEntry> auditEntries = singletonList(entryFor(sameFromDateStatistics));

        given(mockRepository.findAllIpsStatistics()).willReturn(auditEntries);

        IpsStatistics actualStatistics = service.getIpsStatistics(someFromDate, LocalDate.parse("2019-02-28"));
        assertThat(actualStatistics).isEqualTo(NO_STATISTICS);
    }

    @Test
    public void getIpsStatistics_sameToDate_differentFromDate_returnNO_STATISTICS() {
        LocalDate someToDate = LocalDate.parse("2019-03-29");
        IpsStatistics sameToDateStatistics = new IpsStatistics(LocalDate.parse("2019-03-01"), someToDate, ANY_INT, ANY_INT, ANY_INT, ANY_INT);
        List<AuditEntry> auditEntries = singletonList(entryFor(sameToDateStatistics));

        given(mockRepository.findAllIpsStatistics()).willReturn(auditEntries);

        IpsStatistics actualStatistics = service.getIpsStatistics(LocalDate.parse("2018-04-05"), someToDate);
        assertThat(actualStatistics).isEqualTo(NO_STATISTICS);
    }

    @Test
    public void getIpsStatistics_multipleResults_returnRequested() {
        LocalDate someFromDate = LocalDate.parse("2019-08-01");
        LocalDate someToDate = LocalDate.parse("2019-07-31");
        IpsStatistics expectedStatistics = new IpsStatistics(someFromDate, someToDate, ANY_INT, ANY_INT, ANY_INT, ANY_INT);

        List<AuditEntry> auditEntries = asList(
                entryFor(new IpsStatistics(ANY_DATE, ANY_DATE, ANY_INT, ANY_INT, ANY_INT, ANY_INT)),
                entryFor(expectedStatistics),
                entryFor(new IpsStatistics(ANY_DATE, ANY_DATE, ANY_INT, ANY_INT, ANY_INT, ANY_INT)));

        given(mockRepository.findAllIpsStatistics()).willReturn(auditEntries);

        IpsStatistics actualStatistics = service.getIpsStatistics(someFromDate, someToDate);
        assertThat(actualStatistics).isEqualTo(expectedStatistics);
    }
    // TODO OJR EE-20105 malformed -log; malformed - exception
    // TODO OJR EE-20105 Multiple same - log; multiple same - exception

    public AuditEntry entryFor(IpsStatistics notRequestedStatistics) {
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