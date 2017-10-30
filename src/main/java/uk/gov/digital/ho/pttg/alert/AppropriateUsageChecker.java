package uk.gov.digital.ho.pttg.alert;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.alert.sysdig.SuspectUsage;

@Component
public class AppropriateUsageChecker {

    private final Alerter alerter;
    private final IndividualVolumeCheck individualVolumeCheck;
    private final TimeOfRequestCheck timeOfRequestCheck;
    private final MatchingFailureCheck matchingFailureCheck;
    private final boolean alerterStatus;

    public AppropriateUsageChecker(Alerter alerter,
                                   IndividualVolumeCheck individualVolumeCheck,
                                   TimeOfRequestCheck timeOfRequestCheck,
                                   MatchingFailureCheck matchingFailureCheck,
                                   @Value("${alert.enabled}")boolean alerterStatus) {
        this.alerter = alerter;
        this.individualVolumeCheck = individualVolumeCheck;
        this.timeOfRequestCheck = timeOfRequestCheck;
        this.matchingFailureCheck = matchingFailureCheck;
        this.alerterStatus = alerterStatus;
    }

    public SuspectUsage precheck() {
        return check();
    }

    public void postcheck(SuspectUsage suspectUsage) {
        SuspectUsage newSuspectUsage = check();
        if (newSuspectUsage.isSuspect() && !newSuspectUsage.equals(suspectUsage)) {
            if (alerterStatus) {
                alerter.inappropriateUsage(suspectUsage, newSuspectUsage);
            }
        }
    }

    private SuspectUsage check() {
        return new SuspectUsage(
            individualVolumeCheck.check(),
            timeOfRequestCheck.check(),
            matchingFailureCheck.check()
        );
    }
}
