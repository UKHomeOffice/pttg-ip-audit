package uk.gov.digital.ho.pttg.alert.sysdig;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class Event {
    private final String name;
    private final String description;
    private final String severity;
    private final String filter;
    private final Map<String, Object> tags;
}
