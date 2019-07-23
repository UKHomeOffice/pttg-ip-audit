package uk.gov.digital.ho.pttg.api.ipsstatistics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Accessors(fluent = true)
@ToString
class IpsStatistics {
    @JsonProperty(value = "From Date")
    private LocalDate fromDate;
    @JsonProperty(value = "To Date")
    private LocalDate toDate;
    @JsonProperty(value = "Passed")
    private int passed;
    @JsonProperty(value = "Not Passed")
    private int notPassed;
    @JsonProperty(value = "Not Found")
    private int notFound;
    @JsonProperty(value = "Error")
    private int error;
}
