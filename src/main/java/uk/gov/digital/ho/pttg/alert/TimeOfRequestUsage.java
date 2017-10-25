package uk.gov.digital.ho.pttg.alert;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class TimeOfRequestUsage {
    private final long requestsOutsideWorkingDay;

    public TimeOfRequestUsage(long requestsOutsideWorkingDay) {
        this.requestsOutsideWorkingDay = requestsOutsideWorkingDay;
    }

    public boolean isSuspect() {
        return requestsOutsideWorkingDay > 0;
    }

    public long getRequestCount() {
        return requestsOutsideWorkingDay;
    }

    public boolean isWorseThan(TimeOfRequestUsage timeOfRequestUsage) {
        return requestsOutsideWorkingDay > timeOfRequestUsage.requestsOutsideWorkingDay;
    }
}
