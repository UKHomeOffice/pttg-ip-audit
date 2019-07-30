package uk.gov.digital.ho.pttg;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.gov.digital.ho.pttg.api.AuditRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_REQUEST;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE;

@SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
@RunWith(MockitoJUnitRunner.class)
public class AuditHistoryServiceTest {

    @InjectMocks
    private AuditHistoryService auditHistoryService;

    @Mock
    private AuditEntryJpaRepository repository;

    @Test
    public void getAuditHistory_callsAuditRepo() {
        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST);
        Pageable pageable = PageRequest.of(3, 5);
        auditHistoryService.getAuditHistory(LocalDate.now(), eventTypes, pageable);

        verify(repository).findAuditHistory(any(LocalDateTime.class), eq(eventTypes), eq(pageable));
    }

    @Test
    public void getAuditHistory_queriesWithEndOfDay() {
        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST);
        Pageable somePageable = Pageable.unpaged();
        auditHistoryService.getAuditHistory(LocalDate.now(), eventTypes, somePageable);

        verify(repository).findAuditHistory(LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999), eventTypes, somePageable);
    }

    @Test
    public void getAuditHistory_returnsAuditRecords() {
        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST);
        Pageable somePageable = Pageable.unpaged();
        when(repository.findAuditHistory(any(LocalDateTime.class), anyList(), eq(somePageable))).thenReturn(Arrays.asList(getAuditEntry()));

        List<AuditRecord> auditRecords = auditHistoryService.getAuditHistory(LocalDate.now(), eventTypes, somePageable);

        assertThat(auditRecords).size().isEqualTo(1);
        AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord.getId()).isEqualToIgnoringCase("any_corr_id");
        assertThat(auditRecord.getEmail()).isEqualToIgnoringCase("any_user_id");
        assertThat(auditRecord.getNino()).isEqualToIgnoringCase("any_nino");
    }

    @Test
    public void getAllCorrelationIds_givenEventTypes_callsAuditRepo() {
        List<AuditEventType> eventTypes = Collections.singletonList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST);
        auditHistoryService.getAllCorrelationIds(eventTypes);

        verify(repository).getAllCorrelationIds(eq(eventTypes));
    }

    @Test
    public void getAllCorrelationIds_correlationIdsFromRepo_returned() {
        List<String> someCorrelationIds = Arrays.asList("some correlationID", "some other correlationID");
        when(repository.getAllCorrelationIds(any()))
                .thenReturn(someCorrelationIds);

        List<String> returnedCorrelationIds = auditHistoryService.getAllCorrelationIds(
                Collections.singletonList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST));

        assertThat(returnedCorrelationIds).isEqualTo(someCorrelationIds);
    }

    @Test
    public void getAllCorrelationIds_withDate_callsRepoWithEndOfDay() {
        List<AuditEventType> someEventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
        LocalDate someDate = LocalDate.now();

        auditHistoryService.getAllCorrelationIds(someEventTypes, someDate);

        LocalDateTime expectedToDate = LocalDateTime.of(someDate, LocalTime.of(23, 59, 59, 999_999_999));

        then(repository).should().getAllCorrelationIds(eq(someEventTypes), eq(expectedToDate));
    }

    @Test
    public void getAllCorrelationIds_withDate_returnsFromRepo() {
        List<String> expectedCorrelationIds = Arrays.asList("some correlation id", "some other correlation id");
        given(repository.getAllCorrelationIds(anyList(), any(LocalDateTime.class))).willReturn(expectedCorrelationIds);

        List<AuditEventType> anyEventTypes = Collections.singletonList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST);
        LocalDate anyDate = LocalDate.now();
        List<String> actualCorrelationIds = auditHistoryService.getAllCorrelationIds(anyEventTypes, anyDate);

        assertThat(actualCorrelationIds).isEqualTo(expectedCorrelationIds);
    }

    @Test
    public void getRecordsForCorrelationId_givenParameters_callsAuditRepo() {
        String someCorrelationId = "some correlation id";
        List<AuditEventType> someEventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);

        auditHistoryService.getRecordsForCorrelationId(someCorrelationId, someEventTypes);

        verify(repository).findEntriesByCorrelationId(someCorrelationId, someEventTypes);
    }

    @Test
    public void getRecordsForCorrelationId_recordsFromRepo_returned() {
        List<AuditEntry> auditEntries = Collections.singletonList(getAuditEntry());
        when(repository.findEntriesByCorrelationId(any(), any()))
                .thenReturn(auditEntries);

        String someCorrelationId = "some correlation id";
        List<AuditEventType> someEventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
        List<AuditRecord> auditRecords = auditHistoryService.getRecordsForCorrelationId(someCorrelationId, someEventTypes);

        assertThat(auditRecords).size().isEqualTo(1);
        AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord.getId()).isEqualToIgnoringCase("any_corr_id");
        assertThat(auditRecord.getEmail()).isEqualToIgnoringCase("any_user_id");
        assertThat(auditRecord.getNino()).isEqualToIgnoringCase("any_nino");
    }

    private AuditEntry getAuditEntry() {
        return new AuditEntry("some_id",
                LocalDateTime.now(),
                "any_session_id",
                "any_corr_id",
                "any_user_id",
                "any_deployment",
                "any_namespace",
                INCOME_PROVING_FINANCIAL_STATUS_REQUEST,
                "{\"nino\": \"any_nino\"}");
    }


}
