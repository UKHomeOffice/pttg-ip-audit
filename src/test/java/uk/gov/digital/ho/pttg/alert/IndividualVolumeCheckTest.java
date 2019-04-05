package uk.gov.digital.ho.pttg.alert;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.AuditEntryJpaRepository;
import uk.gov.digital.ho.pttg.AuditEventType;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
@RunWith(MockitoJUnitRunner.class)
public class IndividualVolumeCheckTest {

    private IndividualVolumeCheck individualVolumeCheck;
    private Clock clock;

    @Mock private AuditEntryJpaRepository mockRepository;
    @Captor private ArgumentCaptor<LocalDateTime> captorStartTimeCaptor;
    @Captor private ArgumentCaptor<LocalDateTime> captorEndTimeCaptor;

    @Before
    public void before() {
        clock = Clock.fixed(Instant.parse("2017-08-29T08:00:00Z"), ZoneId.of("UTC"));
        individualVolumeCheck = new IndividualVolumeCheck(clock, mockRepository,10, "dev");
    }

    @Test
    public void shouldRetrieveCountsForToday() {
        individualVolumeCheck.check();

        verify(mockRepository).countEntriesBetweenDatesGroupedByUser(captorStartTimeCaptor.capture(), captorEndTimeCaptor.capture(), Mockito.eq(AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE), Mockito.eq("dev"));

        LocalDateTime startTime = captorStartTimeCaptor.getValue();
        LocalDateTime endTime = captorEndTimeCaptor.getValue();

        assertThat(startTime.toLocalDate()).isEqualTo(LocalDate.now(clock));
        assertThat(startTime.getHour()).isEqualTo(0);
        assertThat(startTime.getMinute()).isEqualTo(0);
        assertThat(startTime.getSecond()).isEqualTo(0);

        assertThat(endTime.toLocalDate()).isEqualTo(LocalDate.now(clock).plusDays(1));
        assertThat(endTime.getHour()).isEqualTo(0);
        assertThat(endTime.getMinute()).isEqualTo(0);
        assertThat(endTime.getSecond()).isEqualTo(0);

    }

    @Test
    public void shouldBeSuspectIfAtLeastOneCountIsGreaterThanThreshold() {
        List<CountByUser> twoCountsWithOneOverThreshold = ImmutableList.of(
            new CountByUser(11, "tony"),
            new CountByUser(1, "betty")
        );
        when(mockRepository.countEntriesBetweenDatesGroupedByUser(any(), any(), any(), any())).thenReturn(twoCountsWithOneOverThreshold);


        IndividualVolumeUsage individualVolumeUsage = individualVolumeCheck.check();

        assertThat(individualVolumeUsage.isSuspect()).isTrue();
    }

    @Test
    public void shouldReturnSuspectCountsIfAtLeastOneCountIsGreaterThanThreshold() {
        List<CountByUser> twoCountsWithOneOverThreshold = ImmutableList.of(
            new CountByUser(11, "tony"),
            new CountByUser(1, "betty")
        );
        when(mockRepository.countEntriesBetweenDatesGroupedByUser(any(), any(), any(), any())).thenReturn(twoCountsWithOneOverThreshold);


        IndividualVolumeUsage individualVolumeUsage = individualVolumeCheck.check();

        assertThat(individualVolumeUsage.getCountsByUser()).hasSize(1);
        assertThat(individualVolumeUsage.getCountsByUser()).containsOnlyKeys("tony");
        assertThat(individualVolumeUsage.getCountsByUser()).containsValues(Long.valueOf(11));
    }

    @Test
    public void shouldNotBeSuspectIfCountsAllEqualToThreshold() {
        List<CountByUser> twoCountsWithBothEqualToThreshold = ImmutableList.of(
            new CountByUser(10, "tony"),
            new CountByUser(10, "betty")
        );

        when(mockRepository.countEntriesBetweenDatesGroupedByUser(any(), any(), any(), any())).thenReturn(twoCountsWithBothEqualToThreshold);


        IndividualVolumeUsage individualVolumeUsage = individualVolumeCheck.check();
        assertThat(individualVolumeUsage.isSuspect()).isFalse();
        assertThat(individualVolumeUsage.getCountsByUser()).hasSize(0);
    }

    @Test
    public void shouldNotBeSuspectIfNothingFound() {
        when(mockRepository.countEntriesBetweenDatesGroupedByUser(any(), any(), any(), any())).thenReturn(ImmutableList.of());

        IndividualVolumeUsage individualVolumeUsage = individualVolumeCheck.check();

        assertThat(individualVolumeUsage.isSuspect()).isFalse();
        assertThat(individualVolumeUsage.getCountsByUser()).hasSize(0);
    }

}
