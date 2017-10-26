package uk.gov.digital.ho.pttg.alert;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.alert.sysdig.SuspectUsage;

@Component
public class Alerter {
    private final EventService eventService;

    public Alerter(EventService eventService) {
        this.eventService = eventService;
    }

    public void inappropriateUsage(SuspectUsage previousUsage, SuspectUsage suspectUsage) {
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
