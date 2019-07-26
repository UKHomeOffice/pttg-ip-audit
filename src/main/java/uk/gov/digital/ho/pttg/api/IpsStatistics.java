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
    private static final String FROM_DATE_KEY = "From Date";
    private static final String TO_DATE_KEY = "To Date";
    private static final String PASSED_KEY = "Passed";
    private static final String NOT_PASSED_KEY = "Not Passed";
    private static final String NOT_FOUND_KEY = "Not Found";
    private static final String ERROR_KEY = "Error";

    @JsonProperty(value = FROM_DATE_KEY)
    private LocalDate fromDate;
    @JsonProperty(value = TO_DATE_KEY)
    private LocalDate toDate;
    @JsonProperty(value = PASSED_KEY)
    private int passed;
    @JsonProperty(value = NOT_PASSED_KEY)
    private int notPassed;
    @JsonProperty(value = NOT_FOUND_KEY)
    private int notFound;
    @JsonProperty(value = ERROR_KEY)
    private int error;

    @JsonCreator
    public IpsStatistics(@JsonProperty(value = FROM_DATE_KEY, required = true) @NonNull LocalDate fromDate,
                         @JsonProperty(value = TO_DATE_KEY, required = true) @NonNull LocalDate toDate,
                         @JsonProperty(value = PASSED_KEY, required = true) @NonNull Integer passed,
                         @JsonProperty(value = NOT_PASSED_KEY, required = true) @NonNull Integer notPassed,
                         @JsonProperty(value = NOT_FOUND_KEY, required = true) @NonNull Integer notFound,
                         @JsonProperty(value = ERROR_KEY, required = true) @NonNull Integer error) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.passed = passed;
        this.notPassed = notPassed;
        this.notFound = notFound;
        this.error = error;
    }
}
