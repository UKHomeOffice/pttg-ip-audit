package uk.gov.digital.ho.pttg.alert;

public interface EventService {
    void sendUsersExceedUsageThresholdEvent(IndividualVolumeUsage individualVolumeUsage);
    void sendRequestsOutsideHoursEvent(TimeOfRequestUsage timeOfRequestUsage);
    void sendMatchingFailuresExceedThresholdEvent(MatchingFailureUsage matchingFailureUsage);
}
