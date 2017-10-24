package uk.gov.digital.ho.pttg.alert;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.alert.sysdig.SuspectUsage;
import uk.gov.digital.ho.pttg.alert.sysdig.SysdigEventService;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AlerterTest {

    private Alerter alerter;

    @Mock private SysdigEventService sysdigEventService;

    @Before
    public void before() throws Exception {
        alerter = new Alerter(sysdigEventService);
    }

    @Test
    public void shouldSendIndividualVolumeCheckExceededEventWhenSuspect() throws Exception {
        alerter.inappropriateUsage(noSuspects(), new SuspectUsage(
            new IndividualVolumeUsage(ImmutableMap.of("andy", 11L)),
            new TimeOfRequestUsage(0),
            new MatchingFailureUsage(0)
        ));

        verify(sysdigEventService).sendUsersExceedUsageThresholdEvent(new IndividualVolumeUsage(ImmutableMap.of("andy", 11L)));
    }


    @Test
    public void shouldNotSendIndividualVolumeCheckExceededEventWhenNotSuspect() throws Exception {
        alerter.inappropriateUsage(noSuspects(), new SuspectUsage(
            new IndividualVolumeUsage(ImmutableMap.of()),
            new TimeOfRequestUsage(0),
            new MatchingFailureUsage(0)
        ));

        verify(sysdigEventService, never()).sendUsersExceedUsageThresholdEvent(any());
    }

    @Test
    public void shouldSendMatchingFailuresExceededEventWhenSuspect() throws Exception {
        alerter.inappropriateUsage(noSuspects(), new SuspectUsage(
            new IndividualVolumeUsage(ImmutableMap.of()),
            new TimeOfRequestUsage(0),
            new MatchingFailureUsage(4)
        ));

        verify(sysdigEventService).sendMatchingFailuresExceedThresholdEvent(new MatchingFailureUsage(4));
    }

    @Test
    public void shouldNotSendMatchingFailuresExceededEventWhenNotSuspect() throws Exception {
        alerter.inappropriateUsage(noSuspects(), new SuspectUsage(
            new IndividualVolumeUsage(ImmutableMap.of()),
            new TimeOfRequestUsage(0),
            new MatchingFailureUsage(0)
        ));

        verify(sysdigEventService, never()).sendMatchingFailuresExceedThresholdEvent(any());
    }

    @Test
    public void shouldSendRequestsOutsideHoursEventWhenSuspect() throws Exception {
        alerter.inappropriateUsage(noSuspects(), new SuspectUsage(
            new IndividualVolumeUsage(ImmutableMap.of()),
            new TimeOfRequestUsage(4),
            new MatchingFailureUsage(0)
        ));

        verify(sysdigEventService).sendRequestsOutsideHoursEvent(new TimeOfRequestUsage(4));
    }

    @Test
    public void shouldNotSendRequestsOutsideHoursEventWhenNotSuspect() throws Exception {
        alerter.inappropriateUsage(noSuspects(), new SuspectUsage(
            new IndividualVolumeUsage(ImmutableMap.of()),
            new TimeOfRequestUsage(0),
            new MatchingFailureUsage(0)
        ));

        verify(sysdigEventService, never()).sendRequestsOutsideHoursEvent(any());
    }

    @Test
    public void shouldSendMultipleEventsWhenMultipleSuspectCategories() {
        alerter.inappropriateUsage(noSuspects(), new SuspectUsage(
            new IndividualVolumeUsage(ImmutableMap.of("andy", 11L)),
            new TimeOfRequestUsage(4),
            new MatchingFailureUsage(4)
        ));

        verify(sysdigEventService).sendUsersExceedUsageThresholdEvent(new IndividualVolumeUsage(ImmutableMap.of("andy", 11L)));
        verify(sysdigEventService).sendRequestsOutsideHoursEvent(new TimeOfRequestUsage(4));
        verify(sysdigEventService).sendMatchingFailuresExceedThresholdEvent(new MatchingFailureUsage(4));
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

        verify(sysdigEventService, never()).sendUsersExceedUsageThresholdEvent(any());
        verify(sysdigEventService, never()).sendMatchingFailuresExceedThresholdEvent(any());
        verify(sysdigEventService, never()).sendRequestsOutsideHoursEvent(any());
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


        verify(sysdigEventService).sendUsersExceedUsageThresholdEvent(new IndividualVolumeUsage(ImmutableMap.of("andy", 11L)));
        verify(sysdigEventService).sendRequestsOutsideHoursEvent(new TimeOfRequestUsage(4));
        verify(sysdigEventService).sendMatchingFailuresExceedThresholdEvent(new MatchingFailureUsage(4));
    }


    private SuspectUsage noSuspects() {
        return new SuspectUsage(
            new IndividualVolumeUsage(ImmutableMap.of()),
            new TimeOfRequestUsage(0),
            new MatchingFailureUsage(0)
        );
    }

}
