package uk.gov.digital.ho.pttg.alert;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.alert.sysdig.SuspectUsage;
import uk.gov.digital.ho.pttg.alert.sysdig.SysdigEventService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AlerterTest {

    private Alerter alerter;

    @Mock private SysdigEventService eventService;

    @Before
    public void before() {
        alerter = new Alerter(eventService);
    }

    @Test
    public void shouldSendIndividualVolumeCheckExceededEventWhenSuspect() {
        alerter.inappropriateUsage(noSuspects(), new SuspectUsage(
            new IndividualVolumeUsage(ImmutableMap.of("andy", 11L)),
            new TimeOfRequestUsage(0),
            new MatchingFailureUsage(0)
        ));

        verify(eventService).sendUsersExceedUsageThresholdEvent(new IndividualVolumeUsage(ImmutableMap.of("andy", 11L)));
    }


    @Test
    public void shouldNotSendIndividualVolumeCheckExceededEventWhenNotSuspect() {
        alerter.inappropriateUsage(noSuspects(), new SuspectUsage(
            new IndividualVolumeUsage(ImmutableMap.of()),
            new TimeOfRequestUsage(0),
            new MatchingFailureUsage(0)
        ));

        verify(eventService, never()).sendUsersExceedUsageThresholdEvent(any());
    }

    @Test
    public void shouldSendMatchingFailuresExceededEventWhenSuspect() {
        alerter.inappropriateUsage(noSuspects(), new SuspectUsage(
            new IndividualVolumeUsage(ImmutableMap.of()),
            new TimeOfRequestUsage(0),
            new MatchingFailureUsage(4)
        ));

        verify(eventService).sendMatchingFailuresExceedThresholdEvent(new MatchingFailureUsage(4));
    }

    @Test
    public void shouldNotSendMatchingFailuresExceededEventWhenNotSuspect() {
        alerter.inappropriateUsage(noSuspects(), new SuspectUsage(
            new IndividualVolumeUsage(ImmutableMap.of()),
            new TimeOfRequestUsage(0),
            new MatchingFailureUsage(0)
        ));

        verify(eventService, never()).sendMatchingFailuresExceedThresholdEvent(any());
    }

    @Test
    public void shouldSendRequestsOutsideHoursEventWhenSuspect() {
        alerter.inappropriateUsage(noSuspects(), new SuspectUsage(
            new IndividualVolumeUsage(ImmutableMap.of()),
            new TimeOfRequestUsage(4),
            new MatchingFailureUsage(0)
        ));

        verify(eventService).sendRequestsOutsideHoursEvent(new TimeOfRequestUsage(4));
    }

    @Test
    public void shouldNotSendRequestsOutsideHoursEventWhenNotSuspect() {
        alerter.inappropriateUsage(noSuspects(), new SuspectUsage(
            new IndividualVolumeUsage(ImmutableMap.of()),
            new TimeOfRequestUsage(0),
            new MatchingFailureUsage(0)
        ));

        verify(eventService, never()).sendRequestsOutsideHoursEvent(any());
    }

    @Test
    public void shouldSendMultipleEventsWhenMultipleSuspectCategories() {
        alerter.inappropriateUsage(noSuspects(), new SuspectUsage(
            new IndividualVolumeUsage(ImmutableMap.of("andy", 11L)),
            new TimeOfRequestUsage(4),
            new MatchingFailureUsage(4)
        ));

        verify(eventService).sendUsersExceedUsageThresholdEvent(new IndividualVolumeUsage(ImmutableMap.of("andy", 11L)));
        verify(eventService).sendRequestsOutsideHoursEvent(new TimeOfRequestUsage(4));
        verify(eventService).sendMatchingFailuresExceedThresholdEvent(new MatchingFailureUsage(4));
    }

    @Test
    public void shouldNotSendMultipleEventsWhenNoCategoriesAreWorse() {
        alerter.inappropriateUsage(
            new SuspectUsage(
                new IndividualVolumeUsage(ImmutableMap.of("andy", 11L)),
                new TimeOfRequestUsage(4),
                new MatchingFailureUsage(4)
            ),
            new SuspectUsage(
                new IndividualVolumeUsage(ImmutableMap.of("andy", 11L)),
                new TimeOfRequestUsage(3),
                new MatchingFailureUsage(3)
            )
        );

        verify(eventService, never()).sendUsersExceedUsageThresholdEvent(any());
        verify(eventService, never()).sendMatchingFailuresExceedThresholdEvent(any());
        verify(eventService, never()).sendRequestsOutsideHoursEvent(any());
    }

    @Test
    public void shouldSendMultipleEventsWhenMultipleSuspectCategoriesHaveGotWorse() {
        alerter.inappropriateUsage(
            new SuspectUsage(
                new IndividualVolumeUsage(ImmutableMap.of("sarah", 11L)),
                new TimeOfRequestUsage(3),
                new MatchingFailureUsage(3)
            ),
            new SuspectUsage(
                new IndividualVolumeUsage(ImmutableMap.of("andy", 11L)),
                new TimeOfRequestUsage(4),
                new MatchingFailureUsage(4)
            )
        );

        verify(eventService).sendUsersExceedUsageThresholdEvent(new IndividualVolumeUsage(ImmutableMap.of("andy", 11L)));
        verify(eventService).sendRequestsOutsideHoursEvent(new TimeOfRequestUsage(4));
        verify(eventService).sendMatchingFailuresExceedThresholdEvent(new MatchingFailureUsage(4));
    }


    private SuspectUsage noSuspects() {
        return new SuspectUsage(
            new IndividualVolumeUsage(ImmutableMap.of()),
            new TimeOfRequestUsage(0),
            new MatchingFailureUsage(0)
        );
    }

}
