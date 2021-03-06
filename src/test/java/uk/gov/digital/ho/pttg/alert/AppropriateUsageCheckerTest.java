package uk.gov.digital.ho.pttg.alert;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.alert.sysdig.SuspectUsage;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class AppropriateUsageCheckerTest {

    private AppropriateUsageChecker appropriateUsageChecker;

    @Mock private Alerter alerter;
    @Mock private IndividualVolumeCheck individualVolumeCheck;
    @Mock private TimeOfRequestCheck timeOfRequestCheck;
    @Mock private MatchingFailureCheck matchingFailureCheck;

    @Before
    public void before() {
        appropriateUsageChecker = new AppropriateUsageChecker(alerter, individualVolumeCheck, timeOfRequestCheck, matchingFailureCheck);
    }

    @Test
    public void shouldNotAlertWhenSuspectUsageHasNoSuspectsAndUnchanged() {
        SuspectUsage beforeUsage = new SuspectUsage(new IndividualVolumeUsage(ImmutableMap.of()), new TimeOfRequestUsage(0), new MatchingFailureUsage(0));

        when(individualVolumeCheck.check()).thenReturn(new IndividualVolumeUsage(new HashMap<>()));
        when(timeOfRequestCheck.check()).thenReturn(new TimeOfRequestUsage(0));
        when(matchingFailureCheck.check()).thenReturn(new MatchingFailureUsage(0));

        appropriateUsageChecker.postcheck(beforeUsage);

        verify(alerter, never()).inappropriateUsage(any(), any());
    }

    @Test
    public void shouldNotAlertWhenSuspectUsageHasSuspectsButRemainsUnchanged() {
        SuspectUsage beforeUsage = new SuspectUsage(new IndividualVolumeUsage(ImmutableMap.of("charlie", 6L)), new TimeOfRequestUsage(0), new MatchingFailureUsage(0));

        when(individualVolumeCheck.check()).thenReturn(new IndividualVolumeUsage(Collections.singletonMap("charlie", 6L)));
        when(timeOfRequestCheck.check()).thenReturn(new TimeOfRequestUsage(0));
        when(matchingFailureCheck.check()).thenReturn(new MatchingFailureUsage(0));

        appropriateUsageChecker.postcheck(beforeUsage);

        verify(alerter, never()).inappropriateUsage(any(), any());
    }

    @Test
    public void shouldAlertWhenSuspectUsageHasChangeFromNoSuspectsToHasSuspects() {
        SuspectUsage beforeUsage = new SuspectUsage(new IndividualVolumeUsage(ImmutableMap.of()), new TimeOfRequestUsage(0), new MatchingFailureUsage(0));

        when(individualVolumeCheck.check()).thenReturn(new IndividualVolumeUsage(Collections.singletonMap("charlie", 6L)));
        when(timeOfRequestCheck.check()).thenReturn(new TimeOfRequestUsage(0));
        when(matchingFailureCheck.check()).thenReturn(new MatchingFailureUsage(0));

        appropriateUsageChecker.postcheck(beforeUsage);

        verify(alerter).inappropriateUsage(any(), any());
    }

    @Test
    public void shouldAlertWhenSuspectUsageHasChangeFromSuspectsToHasDifferentSuspects() {
        SuspectUsage beforeUsage = new SuspectUsage(new IndividualVolumeUsage(ImmutableMap.of()), new TimeOfRequestUsage(0), new MatchingFailureUsage(0));

        when(individualVolumeCheck.check()).thenReturn(new IndividualVolumeUsage(Collections.singletonMap("charlie", 7L)));
        when(timeOfRequestCheck.check()).thenReturn(new TimeOfRequestUsage(0));
        when(matchingFailureCheck.check()).thenReturn(new MatchingFailureUsage(0));

        appropriateUsageChecker.postcheck(beforeUsage);

        verify(alerter).inappropriateUsage(any(), any());
    }

    @Test
    public void shouldNotAlertWhenSuspectUsageChangedFromSuspectsToNoSuspects() {
        SuspectUsage beforeUsage = new SuspectUsage(new IndividualVolumeUsage(ImmutableMap.of("charlie", 7L)), new TimeOfRequestUsage(0), new MatchingFailureUsage(0));

        when(individualVolumeCheck.check()).thenReturn(new IndividualVolumeUsage(new HashMap<>()));
        when(timeOfRequestCheck.check()).thenReturn(new TimeOfRequestUsage(0));
        when(matchingFailureCheck.check()).thenReturn(new MatchingFailureUsage(0));

        appropriateUsageChecker.postcheck(beforeUsage);

        verify(alerter, never()).inappropriateUsage(any(), any());
    }

}
