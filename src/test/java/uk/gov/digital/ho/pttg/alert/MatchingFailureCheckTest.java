package uk.gov.digital.ho.pttg.alert;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.AuditEntry;
import uk.gov.digital.ho.pttg.AuditEntryJpaRepository;
import uk.gov.digital.ho.pttg.AuditEventType;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MatchingFailureCheckTest {

    private MatchingFailureCheck matchingFailureCheck;
    private Clock clock;

    @Mock private AuditEntryJpaRepository mockRepository;
    @Captor private ArgumentCaptor<LocalDateTime> captorStartTimeCaptor;
    @Captor private ArgumentCaptor<LocalDateTime> captorEndTimeCaptor;

    @Before
    public void before() throws Exception {
        clock = Clock.fixed(Instant.parse("2017-08-29T08:00:00Z"), ZoneId.of("UTC"));
        matchingFailureCheck = new MatchingFailureCheck(clock, mockRepository,3, "dev");
    }

    @Test
    public void shouldBeSuspectIfMoreThanThresholdFailures() throws Exception {
        List<AuditEntry> fourFailures = ImmutableList.of(
            auditEntryWithDetail("Resource not found"),
            auditEntryWithDetail("Resource not found"),
            auditEntryWithDetail("Resource not found"),
            auditEntryWithDetail("Resource not found")

        );
        when(mockRepository.getEntriesBetweenDates(any(), any(), any(), any())).thenReturn(fourFailures);
        assertThat(matchingFailureCheck.check().isSuspect()).isTrue();
        assertThat(matchingFailureCheck.check().getCountOfFailures()).isEqualTo(4);
    }

    @Test
    public void shouldNotBeSuspectIfSameAsThresholdFailures() throws Exception {
        List<AuditEntry> fourFailures = ImmutableList.of(
            auditEntryWithDetail("Resource not found"),
            auditEntryWithDetail("Resource not found"),
            auditEntryWithDetail("Resource not found")

        );
        when(mockRepository.getEntriesBetweenDates(any(), any(), any(), any())).thenReturn(fourFailures);
        assertThat(matchingFailureCheck.check().isSuspect()).isFalse();
        assertThat(matchingFailureCheck.check().getCountOfFailures()).isEqualTo(0);
    }

    @Test
    public void shouldNotBeSuspectIfManyEntriesButNotFailures() throws Exception {
        List<AuditEntry> fourFailures = ImmutableList.of(
            auditEntryWithDetail("Resource not found"),
            auditEntryWithDetail("Resource not found"),
            auditEntryWithDetail("{}"),
            auditEntryWithDetail("{}"),
            auditEntryWithDetail("{}"),
            auditEntryWithDetail("{}"),
            auditEntryWithDetail("{}"),
            auditEntryWithDetail("{}"),
            auditEntryWithDetail("Resource not found")

        );
        when(mockRepository.getEntriesBetweenDates(any(), any(), any(), any())).thenReturn(fourFailures);
        assertThat(matchingFailureCheck.check().isSuspect()).isFalse();
        assertThat(matchingFailureCheck.check().getCountOfFailures()).isEqualTo(0);
    }

    @Test
    public void shouldRetrieveEntriesInTheLastHour() throws Exception {
        matchingFailureCheck.check();

        verify(mockRepository).getEntriesBetweenDates(captorStartTimeCaptor.capture(), captorEndTimeCaptor.capture(), Mockito.eq(AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE), Mockito.eq("dev"));

        LocalDateTime startTime = captorStartTimeCaptor.getValue();
        LocalDateTime endTime = captorEndTimeCaptor.getValue();

        assertThat(startTime.until(endTime, ChronoUnit.MINUTES)).isEqualTo(60);
        assertThat(endTime.until(LocalDateTime.now(clock), ChronoUnit.MINUTES)).isEqualTo(0);
    }


    private AuditEntry auditEntryWithDetail(String detail) {
        return new AuditEntry(
            UUID.randomUUID().toString(),
            LocalDateTime.now(clock),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            "dave",
            "dev",
            "dev",
            AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
            detail
        );
    }

}
