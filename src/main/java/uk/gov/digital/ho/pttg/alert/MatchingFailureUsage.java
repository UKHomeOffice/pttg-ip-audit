package uk.gov.digital.ho.pttg.alert;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class MatchingFailureUsage {
    private final long noMatchesCountForPeriod;

    public MatchingFailureUsage(long noMatchesCountForPeriod) {
        this.noMatchesCountForPeriod = noMatchesCountForPeriod;
    }

    public boolean isSuspect() {
        return noMatchesCountForPeriod > 0;
    }

    public long getCountOfFailures() {
        return noMatchesCountForPeriod;
    }

    public boolean isWorseThan(MatchingFailureUsage matchingFailureUsage) {
        return noMatchesCountForPeriod > matchingFailureUsage.noMatchesCountForPeriod;
    }
}
