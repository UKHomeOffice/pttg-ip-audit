package uk.gov.digital.ho.pttg;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;
import uk.gov.digital.ho.pttg.api.AuditRecord;
import uk.gov.digital.ho.pttg.api.AuditResource;
import uk.gov.digital.ho.pttg.api.AuditableData;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class AuditResourceTest {

    private static final AuditRecord AUDIT_RECORD = new AuditRecord("some id",
            LocalDateTime.of(2017, 12, 8, 0, 0),
            "some email",
            AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_REQUEST,
            ImmutableMap.of("some key", "some value"),
            "some nino"
    );

    @Mock private AuditService mockService;

    private AuditResource resource;

    @Before
    public void setUp() {
        resource = new AuditResource(mockService);
    }

    @Test
    public void shouldUseCollaboratorsForRetrieveAllAuditData() {

        when(mockService.getAllAuditData(any(Pageable.class))).thenReturn(Collections.singletonList(AUDIT_RECORD));

        resource.retrieveAllAuditData(null);

        verify(mockService).getAllAuditData(null);
    }

    @Test
    public void shouldUsePaginationObjectForRetrieveAllAuditData() {

        Pageable pageable = mock(Pageable.class);

        when(mockService.getAllAuditData(pageable)).thenReturn(Collections.singletonList(AUDIT_RECORD));

        resource.retrieveAllAuditData(pageable);

        verify(mockService).getAllAuditData(pageable);
    }

    @Test
    public void shouldUseReturnAuditRecords() {

        when(mockService.getAllAuditData(null)).thenReturn(Collections.singletonList(AUDIT_RECORD));

        List<AuditRecord> auditRecords = resource.retrieveAllAuditData(null);

        assertThat(auditRecords).containsExactly(AUDIT_RECORD);
    }

    @Test
    public void shouldUseCollaboratorsForRecordAuditEntry() {

        AuditableData auditableData = new AuditableData("some event id",
                LocalDateTime.of(2017,12, 8, 0, 0),
                "some session id",
                "some correlation id",
                "some user id",
                "some deployment name",
                "some deployment namespace",
                AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
                "some data");

        resource.recordAuditEntry(auditableData);

        verify(mockService).add(auditableData);
    }

}