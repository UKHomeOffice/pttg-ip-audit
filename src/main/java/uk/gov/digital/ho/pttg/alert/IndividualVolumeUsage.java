package uk.gov.digital.ho.pttg.alert;

import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode
public class IndividualVolumeUsage {
    private final Map<String, Long> exceededVolumeByUser;

    public IndividualVolumeUsage(Map<String, Long> exceededVolumeByUser) {
        this.exceededVolumeByUser = exceededVolumeByUser;
    }

    public boolean isSuspect() {
        return !exceededVolumeByUser.isEmpty();
    }

    public Map<String, Long> getCountsByUser() {
        return exceededVolumeByUser;
    }

    public boolean isWorseThan(IndividualVolumeUsage individualVolumeUsage) {
        return !this.equals(individualVolumeUsage);
    }
}
