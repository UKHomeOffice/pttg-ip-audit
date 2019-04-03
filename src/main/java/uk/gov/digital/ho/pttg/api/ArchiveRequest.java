package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
@AllArgsConstructor
class   ArchiveRequest {
    @JsonProperty(required = true) @NonNull
    private LocalDate lastArchiveDate;
    @JsonProperty(required = true) @NonNull
    private List<String> eventIds;
    @JsonProperty(required = true) @NonNull
    private LocalDate resultDate;
}
