package uk.gov.digital.ho.pttg.alert;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.AuditEntryJpaRepository;
import uk.gov.digital.ho.pttg.AuditEventType;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

@Component
public class TimeOfRequestCheck {

    private final Clock clock;
    private final AuditEntryJpaRepository repository;
    private final String startTime;
    private final String endTime;
    private final String namespace;

    public TimeOfRequestCheck(Clock clock,
                              AuditEntryJpaRepository repository,
                              @Value("${alert.acceptable.hours.start}") String startTime,
                                @Value("${alert.acceptable.hours.end}") String endTime,
                                @Value("${auditing.deployment.namespace}") String namespace) {

        this.clock = clock;
        this.repository = repository;
        this.startTime = startTime;
        this.endTime = endTime;
        this.namespace = namespace;
    }

    public TimeOfRequestUsage check() {

        Long beforeWorkingHours = repository.countEntriesBetweenDates(
            LocalDate.now(clock).atStartOfDay(),
            LocalDate.now(clock).atTime(getStartHour(), getStartMinute()),
            AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
            namespace);

        Long afterWorkingHours = repository.countEntriesBetweenDates(
            LocalDate.now(clock).atTime(getEndHour(), getEndMinute()),
            LocalDate.now(clock).atStartOfDay().plusDays(1),
            AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
            namespace);

        return new TimeOfRequestUsage(beforeWorkingHours + afterWorkingHours);
    }

    private int getEndHour() {
        return parseLondonTime(endTime).getHour();
    }

    private int getEndMinute() {
        return parseLondonTime(endTime).getMinute();
    }

    private int getStartMinute() {
        return parseLondonTime(startTime).getMinute();
    }

    private int getStartHour() {
        return parseLondonTime(startTime).getHour();
    }

    private LocalTime parseLondonTime(String time) {

        // config is specified as a UK time e.g 07:00 is 07:00 UTC in winter and 06:00 UTC in summer
        return LocalDate.now(clock)
                        .atTime(LocalTime.parse(time))
                        .atZone(ZoneId.of("Europe/London"))
                        .withZoneSameInstant(ZoneId.systemDefault())
                        .toLocalTime();
    }
}
