package uk.gov.digital.ho.pttg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.dto.AuditRecord;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.pttg.AuditEntryRepositoryTest.*;


@RunWith(MockitoJUnitRunner.class)
public class AuditEntryResourceTest {


    @Mock
    private AuditEntryJpaRepository mockRepo;
    private AuditResource resource;

    @Before
    public void setUp() {
        resource = new AuditResource(mockRepo, new ObjectMapper());
    }

    @Test
    public void testCollaboratorsGettingAudit() {


        when(mockRepo.findAllByOrderByTimestampDesc()).thenReturn(buildAuditList());

        List<AuditRecord> auditRecords = resource.allAudit();

        verify(mockRepo).findAllByOrderByTimestampDesc();

        assertThat(auditRecords.size()).isEqualTo(2);
        assertThat(auditRecords.get(0).getId()).isEqualTo("correlationId");
        assertThat(auditRecords.get(1).getId()).isEqualTo("correlationId");
    }

    private List<AuditEntry> buildAuditList() {
        return ImmutableList.of(createAudit(NOW), createAudit(NOW_PLUS_60_MINS));
    }

    private AuditEntry createAudit(LocalDateTime timestamp) {
        AuditEntry auditEntry = new AuditEntry(
                UUID,
                timestamp,
                SESSION_ID,
                "correlationId",
                USER_ID,
                DEPLOYMENT,
                NAMESPACE,
                "INCOME_PROVING_FINANCIAL_STATUS_RESPONSE",
                DETAIL
        );
        return auditEntry;
    }
}