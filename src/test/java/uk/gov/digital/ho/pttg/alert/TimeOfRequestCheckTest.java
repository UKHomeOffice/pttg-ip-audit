package uk.gov.digital.ho.pttg.alert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.AuditEntryJpaRepository;

import java.time.*;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE;

@RunWith(MockitoJUnitRunner.class)
public class TimeOfRequestCheckTest {
    private TimeOfRequestCheck timeOfRequestCheck;
    @Mock
    private AuditEntryJpaRepository repository;
    @Captor
    private ArgumentCaptor<LocalDateTime> startTimeCaptor;
    @Captor
    private ArgumentCaptor<LocalDateTime> endTimeCaptor;

    private static TimeZone defaultTimeZone;

    @BeforeClass
    public static void beforeAllTests() {
        defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @AfterClass
    public static void afterAllTests() {
        TimeZone.setDefault(defaultTimeZone);
    }

    @Before
    public void before() throws Exception {
        timeOfRequestCheck = new TimeOfRequestCheck("07:23", "19:13", "dev", Clock.fixed(Instant.parse("2017-08-29T08:00:00Z"), ZoneId.of("UTC")));
    }

    @Test
    public void shouldCountRequestsBetweenStartOfDayAndStartOfWorkingDayInUTCWhenInDaylightSavings() throws Exception {
        timeOfRequestCheck.check(repository);

        verify(repository, atLeast(2)).countEntriesBetweenDates(startTimeCaptor.capture(), endTimeCaptor.capture(), Mockito.eq(INCOME_PROVING_FINANCIAL_STATUS_RESPONSE), Mockito.eq("dev"));

        LocalDateTime startOfDay = startTimeCaptor.getAllValues().get(0);
        LocalDateTime startOfWorkingDay = endTimeCaptor.getAllValues().get(0);

        assertThat(startOfDay.toLocalDate()).isEqualTo(LocalDate.of(2017, 8, 29));
        assertThat(startOfDay.getHour()).isEqualTo(0);
        assertThat(startOfDay.getMinute()).isEqualTo(0);
        assertThat(startOfDay.getSecond()).isEqualTo(0);

        assertThat(startOfWorkingDay.toLocalDate()).isEqualTo(LocalDate.of(2017, 8, 29));
        assertThat(startOfWorkingDay.getHour()).isEqualTo(6);
        assertThat(startOfWorkingDay.getMinute()).isEqualTo(23);
        assertThat(startOfWorkingDay.getSecond()).isEqualTo(0);

    }

    @Test
    public void shouldCountRequestsBetweenStartOfDayAndStartOfWorkingDayInUTCWhenNotInDaylightSavings() throws Exception {
        timeOfRequestCheck = new TimeOfRequestCheck("07:23", "19:13", "dev", Clock.fixed(Instant.parse("2017-12-29T08:00:00Z"), ZoneId.of("UTC")));
        timeOfRequestCheck.check(repository);

        verify(repository, atLeast(2)).countEntriesBetweenDates(startTimeCaptor.capture(), endTimeCaptor.capture(), Mockito.eq(INCOME_PROVING_FINANCIAL_STATUS_RESPONSE), Mockito.eq("dev"));

        LocalDateTime startOfDay = startTimeCaptor.getAllValues().get(0);
        LocalDateTime startOfWorkingDay = endTimeCaptor.getAllValues().get(0);

        assertThat(startOfDay.toLocalDate()).isEqualTo(LocalDate.of(2017, 12, 29));
        assertThat(startOfDay.getHour()).isEqualTo(0);
        assertThat(startOfDay.getMinute()).isEqualTo(0);
        assertThat(startOfDay.getSecond()).isEqualTo(0);

        assertThat(startOfWorkingDay.toLocalDate()).isEqualTo(LocalDate.of(2017, 12, 29));
        assertThat(startOfWorkingDay.getHour()).isEqualTo(7);
        assertThat(startOfWorkingDay.getMinute()).isEqualTo(23);
        assertThat(startOfWorkingDay.getSecond()).isEqualTo(0);

    }

    @Test
    public void shouldCountRequestsBetweenEndOfWorkingDayInUTCAndStartOfTomorrowWhenInDaylightSavings() throws Exception {
        timeOfRequestCheck.check(repository);

        verify(repository, atLeast(2)).countEntriesBetweenDates(startTimeCaptor.capture(), endTimeCaptor.capture(), Mockito.eq(INCOME_PROVING_FINANCIAL_STATUS_RESPONSE), Mockito.eq("dev"));

        LocalDateTime endOfWorkingDay = startTimeCaptor.getAllValues().get(1);
        LocalDateTime endOfDay = endTimeCaptor.getAllValues().get(1);

        assertThat(endOfWorkingDay.toLocalDate()).isEqualTo(LocalDate.of(2017, 8, 29));
        assertThat(endOfWorkingDay.getHour()).isEqualTo(18);
        assertThat(endOfWorkingDay.getMinute()).isEqualTo(13);
        assertThat(endOfWorkingDay.getSecond()).isEqualTo(0);

        assertThat(endOfDay.toLocalDate()).isEqualTo(LocalDate.of(2017, 8, 30));
        assertThat(endOfDay.getHour()).isEqualTo(0);
        assertThat(endOfDay.getMinute()).isEqualTo(0);
        assertThat(endOfDay.getSecond()).isEqualTo(0);
    }

    @Test
    public void shouldCountRequestsBetweenEndOfWorkingDayInUTCAndStartOfTomorrowWhenNotInDaylightSavings() throws Exception {
        timeOfRequestCheck = new TimeOfRequestCheck("07:23", "19:13", "dev", Clock.fixed(Instant.parse("2017-12-29T08:00:00Z"), ZoneId.of("UTC")));
        timeOfRequestCheck.check(repository);

        verify(repository, atLeast(2)).countEntriesBetweenDates(startTimeCaptor.capture(), endTimeCaptor.capture(), Mockito.eq(INCOME_PROVING_FINANCIAL_STATUS_RESPONSE), Mockito.eq("dev"));

        LocalDateTime endOfWorkingDay = startTimeCaptor.getAllValues().get(1);
        LocalDateTime endOfDay = endTimeCaptor.getAllValues().get(1);

        assertThat(endOfWorkingDay.toLocalDate()).isEqualTo(LocalDate.of(2017, 12, 29));
        assertThat(endOfWorkingDay.getHour()).isEqualTo(19);
        assertThat(endOfWorkingDay.getMinute()).isEqualTo(13);
        assertThat(endOfWorkingDay.getSecond()).isEqualTo(0);

        assertThat(endOfDay.toLocalDate()).isEqualTo(LocalDate.of(2017, 12, 30));
        assertThat(endOfDay.getHour()).isEqualTo(0);
        assertThat(endOfDay.getMinute()).isEqualTo(0);
        assertThat(endOfDay.getSecond()).isEqualTo(0);
    }

    @Test
    public void shouldSumBothCounts() {
        when(repository.countEntriesBetweenDates(any(), any(), any(), any())).thenReturn(Long.valueOf(3));

        TimeOfRequestUsage timeOfRequestUsage = timeOfRequestCheck.check(repository);

        assertThat(timeOfRequestUsage.getRequestCount()).isEqualTo(6);
    }
}
