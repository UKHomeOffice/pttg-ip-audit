package uk.gov.digital.ho.pttg;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;
import uk.gov.digital.ho.pttg.dto.AuditCsvView;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.pttg.AuditRepositoryTest.*;


@RunWith(MockitoJUnitRunner.class)
public class AuditResourceTest {


    @Mock
    private AuditRepository mockRepo;

    @Mock
    private Model mockModel;

    @InjectMocks
    private AuditResource resource;

    @Before
    public void setUp() {

    }

    @Test
    public void testCollaboratorsGettingAudit() {


        when(mockRepo.findAllByOrderByTimestampDesc()).thenReturn(buildAuditList());

        resource.allAudit(mockModel);

        verify(mockRepo).findAllByOrderByTimestampDesc();

        verify(mockModel).addAttribute("audit", buildAuditViewList());
    }

    private List<Audit> buildAuditList() {
        return ImmutableList.of(createAudit(NOW), createAudit(NOW_PLUS_60_MINS));
    }

    private Audit createAudit(LocalDateTime timestamp) {
        Audit auditEntry = new Audit(
                UUID,
                timestamp,
                SESSION_ID,
                UUID,
                USER_ID,
                DEPLOYMENT,
                NAMESPACE,
                AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
                DETAIL
        );
        return auditEntry;
    }


    private List<AuditCsvView> buildAuditViewList() {
        return ImmutableList.of(createAuditView(NOW), createAuditView(NOW_PLUS_60_MINS));
    }

    private AuditCsvView createAuditView(LocalDateTime timestamp) {
        return AuditCsvView.builder().detail(DETAIL)
                .timestamp(timestamp)
                .userId(USER_ID)
                .build();
    }

}