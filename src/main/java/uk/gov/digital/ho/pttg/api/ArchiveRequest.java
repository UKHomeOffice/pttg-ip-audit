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
public class ArchiveRequest {
    @JsonProperty
    private String result;
    @JsonProperty
    private LocalDate lastArchiveDate;
    @JsonProperty
    private List<String> correlationIds;
    @JsonProperty
    private String nino;

    @JsonCreator
    ArchiveRequest(
            @JsonProperty(value = "result", required = true) @NonNull String result,
            @JsonProperty(value = "lastArchiveDate", required = true) @NonNull LocalDate lastArchiveDate,
            @JsonProperty(value = "correlationIds", required = true) @NonNull List<String> correlationIds,
            @JsonProperty(value = "nino", required = true) @NonNull String nino
    ) {
        this.result = result;
        this.lastArchiveDate = lastArchiveDate;
        this.correlationIds = correlationIds;
        this.nino = nino;

        validate();
    }

    private void validate() {
        if (result.isEmpty()) {
            throwApiError("A result is required to record in the archive");
        }

        if (correlationIds.isEmpty()) {
            throwApiError("At least one event id is required to be deleted from the audit history");
        }

        if (nino.isEmpty()) {
            throwApiError("A nino is required to check for newer requests and prevent archiving");
        }
    }

    private void throwApiError(String error) {
        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, error);
    }

}
