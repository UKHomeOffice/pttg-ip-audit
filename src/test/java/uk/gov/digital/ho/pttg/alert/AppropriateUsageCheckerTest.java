package uk.gov.digital.ho.pttg.alert;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.AuditEntryJpaRepository;
import uk.gov.digital.ho.pttg.alert.sysdig.SuspectUsage;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AppropriateUsageCheckerTest {

    private AppropriateUsageChecker appropriateUsageChecker;

    @Mock private AuditEntryJpaRepository repository;
    @Mock private Alerter alerter;
    @Mock private IndividualVolumeCheck individualVolumeCheck;
    @Mock private TimeOfRequestCheck timeOfRequestCheck;
    @Mock private MatchingFailureCheck matchingFailureCheck;

    @Before
    public void before() throws Exception {
        appropriateUsageChecker = new AppropriateUsageChecker(repository, alerter, individualVolumeCheck, timeOfRequestCheck, matchingFailureCheck);
    }

    @Test
    public void shouldNotAlertWhenSuspectUsageHasNoSuspectsAndUnchanged() throws Exception {
        SuspectUsage beforeUsage = new SuspectUsage(new IndividualVolumeUsage(ImmutableMap.of()), new TimeOfRequestUsage(0), new MatchingFailureUsage(0));

        when(individualVolumeCheck.check(repository)).thenReturn(new IndividualVolumeUsage(new HashMap<>()));
        when(timeOfRequestCheck.check(repository)).thenReturn(new TimeOfRequestUsage(0));
        when(matchingFailureCheck.check(repository)).thenReturn(new MatchingFailureUsage(0));

        appropriateUsageChecker.postcheck(beforeUsage);

        verify(alerter, never()).inappropriateUsage(any(), any());
    }

    @Test
    public void shouldNotAlertWhenSuspectUsageHasSuspectsButRemainsUnchanged() throws Exception {
        SuspectUsage beforeUsage = new SuspectUsage(new IndividualVolumeUsage(ImmutableMap.of("charlie", 6l)), new TimeOfRequestUsage(0), new MatchingFailureUsage(0));

        when(individualVolumeCheck.check(repository)).thenReturn(new IndividualVolumeUsage(Collections.singletonMap("charlie", 6l)));
        when(timeOfRequestCheck.check(repository)).thenReturn(new TimeOfRequestUsage(0));
        when(matchingFailureCheck.check(repository)).thenReturn(new MatchingFailureUsage(0));

        appropriateUsageChecker.postcheck(beforeUsage);

        verify(alerter, never()).inappropriateUsage(any(), any());
    }

    @Test
    public void shouldAlertWhenSuspectUsageHasChangeFromNoSuspectsToHasSuspects() throws Exception {
        SuspectUsage beforeUsage = new SuspectUsage(new IndividualVolumeUsage(ImmutableMap.of()), new TimeOfRequestUsage(0), new MatchingFailureUsage(0));

        when(individualVolumeCheck.check(repository)).thenReturn(new IndividualVolumeUsage(Collections.singletonMap("charlie", 6l)));
        when(timeOfRequestCheck.check(repository)).thenReturn(new TimeOfRequestUsage(0));
        when(matchingFailureCheck.check(repository)).thenReturn(new MatchingFailureUsage(0));

        appropriateUsageChecker.postcheck(beforeUsage);

        verify(alerter).inappropriateUsage(any(), any());
    }

    @Test
    public void shouldAlertWhenSuspectUsageHasChangeFromSuspectsToHasDifferentSuspects() throws Exception {
        SuspectUsage beforeUsage = new SuspectUsage(new IndividualVolumeUsage(ImmutableMap.of()), new TimeOfRequestUsage(0), new MatchingFailureUsage(0));

        when(individualVolumeCheck.check(repository)).thenReturn(new IndividualVolumeUsage(Collections.singletonMap("charlie", 7l)));
        when(timeOfRequestCheck.check(repository)).thenReturn(new TimeOfRequestUsage(0));
        when(matchingFailureCheck.check(repository)).thenReturn(new MatchingFailureUsage(0));

        appropriateUsageChecker.postcheck(beforeUsage);

        verify(alerter).inappropriateUsage(any(), any());
    }

    @Test
    public void shouldNotAlertWhenSuspectUsageChangedFromSuspectsToNoSuspects() throws Exception {
        SuspectUsage beforeUsage = new SuspectUsage(new IndividualVolumeUsage(ImmutableMap.of("charlie", 7l)), new TimeOfRequestUsage(0), new MatchingFailureUsage(0));

        when(individualVolumeCheck.check(repository)).thenReturn(new IndividualVolumeUsage(new HashMap<>()));
        when(timeOfRequestCheck.check(repository)).thenReturn(new TimeOfRequestUsage(0));
        when(matchingFailureCheck.check(repository)).thenReturn(new MatchingFailureUsage(0));

        appropriateUsageChecker.postcheck(beforeUsage);

        verify(alerter, never()).inappropriateUsage(any(), any());
    }

}
