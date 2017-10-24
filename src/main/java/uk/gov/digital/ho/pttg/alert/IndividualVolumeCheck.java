package uk.gov.digital.ho.pttg.alert;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.AuditEntryJpaRepository;
import uk.gov.digital.ho.pttg.AuditEventType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class IndividualVolumeCheck {
    private final int dailyUsageThreshold;
    private final String namespace;

    public IndividualVolumeCheck(@Value("${alert.individual.usage.daily.threshold}") int dailyUsageThreshold,
                                 @Value("${auditing.deployment.namespace}") String namespace) {
        this.dailyUsageThreshold = dailyUsageThreshold;
        this.namespace = namespace;
    }

    public IndividualVolumeUsage check(AuditEntryJpaRepository repository) {
        List<CountByUser> counts = repository.countEntriesBetweenDatesGroupedByUser(LocalDate.now().atStartOfDay(), LocalDate.now().atStartOfDay().plusDays(1), AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE, namespace);
        return new IndividualVolumeUsage(countsOverThreshold(counts));
    }

    private Map<String, Long> countsOverThreshold(List<CountByUser> counts) {
        return counts
            .stream()
            .filter(countByUser -> countByUser.getCount() > dailyUsageThreshold)
            .collect(Collectors.toMap(CountByUser::getUserId, CountByUser::getCount));
    }
}
