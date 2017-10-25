package uk.gov.digital.ho.pttg.alert;

import lombok.Getter;

@Getter
public class CountByUser {
    private final long count;
    private final String userId;

    public CountByUser(long count, String userId) {
        this.count = count;
        this.userId = userId;
    }
}
