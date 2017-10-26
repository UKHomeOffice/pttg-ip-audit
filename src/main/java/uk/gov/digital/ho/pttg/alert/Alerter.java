package uk.gov.digital.ho.pttg.alert;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.alert.sysdig.SuspectUsage;
import uk.gov.digital.ho.pttg.alert.sysdig.SysdigEventService;

@Component
public class Alerter {
    private final SysdigEventService eventService;

    public Alerter(SysdigEventService eventService) {
        this.eventService = eventService;
    }

    void inappropriateUsage(SuspectUsage previousUsage, SuspectUsage suspectUsage) {
        if (suspectUsage.getIndividualVolumeUsage().isSuspect() &&
            suspectUsage.getIndividualVolumeUsage().isWorseThan(previousUsage.getIndividualVolumeUsage())) {
            eventService.sendUsersExceedUsageThresholdEvent(suspectUsage.getIndividualVolumeUsage());
        }

        if (suspectUsage.getMatchingFailureUsage().isSuspect() &&
            suspectUsage.getMatchingFailureUsage().isWorseThan(previousUsage.getMatchingFailureUsage())) {
            eventService.sendMatchingFailuresExceedThresholdEvent(suspectUsage.getMatchingFailureUsage());
        }

        if (suspectUsage.getTimeOfRequestUsage().isSuspect() &&
            suspectUsage.getTimeOfRequestUsage().isWorseThan(previousUsage.getTimeOfRequestUsage())) {
            eventService.sendRequestsOutsideHoursEvent(suspectUsage.getTimeOfRequestUsage());
        }
    }
}
