package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
class ArchiveRequest {
    @JsonProperty
    private String result;
    @JsonProperty
    private LocalDate lastArchiveDate;
    @JsonProperty
    private List<String> eventIds;

    @JsonCreator
    ArchiveRequest(
            @JsonProperty(value = "result", required = true) @NonNull String result,
            @JsonProperty(value = "lastArchiveDate", required = true) @NonNull LocalDate lastArchiveDate,
            @JsonProperty(value = "eventIds", required = true) @NonNull List<String> eventIds
    ) {
        this.result = result;
        this.lastArchiveDate = lastArchiveDate;
        this.eventIds = eventIds;

        validate();
    }

    private void validate() {
        if (result.isEmpty()) {
            throwApiError("A result is required to record in the archive");
        }

        if (eventIds.isEmpty()) {
            throwApiError("At least one event id is required to be deleted from the audit history");
        }
    }

    private void throwApiError(String error) {
        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, error);
    }

}
