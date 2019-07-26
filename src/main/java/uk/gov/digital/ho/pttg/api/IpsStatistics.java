package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@EqualsAndHashCode
@Getter
@Accessors(fluent = true)
@ToString
public
class IpsStatistics {
    @JsonProperty
    private LocalDate fromDate;
    @JsonProperty
    private LocalDate toDate;
    @JsonProperty
    private int passed;
    @JsonProperty
    private int notPassed;
    @JsonProperty
    private int notFound;
    @JsonProperty
    private int error;

    @JsonCreator
    public IpsStatistics(@JsonProperty(value = "From Date", required = true) @NonNull LocalDate fromDate,
                         @JsonProperty(value = "To Date", required = true) @NonNull LocalDate toDate,
                         @JsonProperty(value = "Passed", required = true) @NonNull Integer passed,
                         @JsonProperty(value = "Not Passed", required = true) @NonNull Integer notPassed,
                         @JsonProperty(value = "Not Found", required = true) @NonNull Integer notFound,
                         @JsonProperty(value = "Error", required = true) @NonNull Integer error) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.passed = passed;
        this.notPassed = notPassed;
        this.notFound = notFound;
        this.error = error;
    }
}
