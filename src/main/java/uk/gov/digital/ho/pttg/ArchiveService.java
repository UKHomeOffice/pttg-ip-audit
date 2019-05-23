package uk.gov.digital.ho.pttg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.pttg.api.ArchivedResult;
import uk.gov.digital.ho.pttg.application.ArchiveException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.pttg.AuditEventType.ARCHIVED_RESULTS;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@Component
@Slf4j
public class ArchiveService {

    private final AuditEntryJpaRepository repository;
    private final ObjectMapper objectMapper;

    public ArchiveService(AuditEntryJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void handleArchiveRequest(LocalDate resultDate, String result, List<String> correlationIds, LocalDate lastArchiveDate, String nino) {
        LocalDateTime lastArchiveTime = lastArchiveDate.atTime(23, 59, 59, 999999999);
        if (repository.countNinosAfterDate(lastArchiveTime, nino) > 0) {
            log.info("Archive request ignored as more recent requests found for nino", value(EVENT, PTTG_AUDIT_ARCHIVE_NOT_READY_FOR_NINO));
            return;
        }
        repository.deleteAllCorrelationIds(correlationIds);
        archiveResult(resultDate, result);
    }

    void archiveResult(LocalDate date, String result) {
        List<AuditEntry> existingResults = repository.findArchivedResults(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        AuditEntry newResult = addResult(date, result, existingResults);
        repository.save(newResult);
    }

    public List<ArchivedResult> getArchivedResults(LocalDate fromDate, LocalDate toDate) {
        List<AuditEntry> archivedResults = repository.findArchivedResults(fromDate.atStartOfDay(), toDate.plusDays(1).atStartOfDay());

        return archivedResults.stream()
                .map(this::deserializeArchiveResultDetail)
                .collect(Collectors.toList());
    }

    private AuditEntry addResult(LocalDate date, String result, List<AuditEntry> existingResults) {
        AuditEntry newResult = null;
        if (existingResults.size() == 0) {
            newResult = createArchivedResult(date, result);
        }
        else if (existingResults.size() == 1){
            newResult = incrementArchivedResult(existingResults.get(0), result);
        }
        else {
            handleError(String.format("Found multiple archives for a single date: %s, archives: %s", date, existingResults));
        }
        return newResult;
    }

    AuditEntry createArchivedResult(LocalDate date, String result) {
        Map<String, Integer> count = new HashMap<>();
        count.put(result, 1);
        ArchivedResult archivedResult = new ArchivedResult(count);
        String detailString = serializeArchiveResult(archivedResult);
        return new AuditEntry(
                UUID.randomUUID().toString(),
                date.atStartOfDay(),
                "",
                UUID.randomUUID().toString(),
                "Audit Service",
                "",
                "",
                ARCHIVED_RESULTS,
                detailString
        );
    }

    AuditEntry incrementArchivedResult(AuditEntry existingResult, String result) {
        ArchivedResult existingArchive = deserializeArchiveResultDetail(existingResult);

        Map<String, Integer> newResult = existingArchive.getResults();
        if (newResult == null) {
            handleError(String.format("Unable to find results in archive result detail: %s", existingArchive));
        }
        int existingCount = newResult.getOrDefault(result, 0);
        newResult.put(result, existingCount + 1);
        String newResultString = serializeArchiveResultDetail(existingArchive, newResult);
        existingResult.setDetail(newResultString);
        return existingResult;
    }

    private String serializeArchiveResult(ArchivedResult archivedResult) {
        String detailString = "";
        try {
            detailString = objectMapper.writeValueAsString(archivedResult);
        } catch (JsonProcessingException e) {
            handleException(String.format("Failed to serialize archivedResult %s", archivedResult), e);
        }
        return detailString;
    }

    private String serializeArchiveResultDetail(ArchivedResult existingArchive, Map<String, Integer> newResult) {
        String newResultString = "";
        try {
            newResultString = objectMapper.writeValueAsString(existingArchive);
        } catch (JsonProcessingException e) {
            handleException(String.format("Failed to serialize updated archivedResult.detail %s", newResult), e);
        }
        return newResultString;
    }

    private ArchivedResult deserializeArchiveResultDetail(AuditEntry existingResult) {
        ArchivedResult existingArchive = new ArchivedResult(new HashMap<>());
        try {
            existingArchive = objectMapper.readValue(existingResult.getDetail(), ArchivedResult.class);
        } catch (IOException e) {
            handleException(String.format("Failed to deserialize archivedResult.detail %s", existingResult.getDetail()), e);
        }
        return existingArchive;
    }

    private void handleError(String errorMessage) {
        log.error(errorMessage, value(EVENT, PTTG_AUDIT_ARCHIVE_FAILURE));
        throw new ArchiveException(errorMessage);
    }

    private void handleException(String errorMessage, Exception exception) {
        log.error(errorMessage, exception, value(EVENT, PTTG_AUDIT_ARCHIVE_FAILURE));
        throw new ArchiveException(errorMessage, exception);
    }
}
