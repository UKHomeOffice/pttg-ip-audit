package uk.gov.digital.ho.pttg;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest
@Profile("logtoconsole")
public class AuditRepositoryTest {

    static final String SESSION_ID = "sessionID";
    static final String DEPLOYMENT = "deployment";
    static final String USER_ID = "me";
    static final String UUID = "uuid";
    static final String NAMESPACE = "env";
    static final String DETAIL = "{}";
    static LocalDateTime NOW = LocalDateTime.now();
    static LocalDateTime NOW_MINUS_60_MINS = LocalDateTime.now().minusMinutes(60);
    static LocalDateTime NOW_PLUS_60_MINS = LocalDateTime.now().plusMinutes(60);



    @Autowired
    private AuditRepository repository;

    @Before
    public void setup() {
        repository.save(createAudit(NOW));
        repository.save(createAudit(NOW_MINUS_60_MINS));
        repository.save(createAudit(NOW_PLUS_60_MINS));
    }



    @Test
    public void shouldRetrieveAllAudit() {

        final Iterable<Audit> all = repository.findAll();
        assertThat(all).size().isEqualTo(3);
    }

    @Test
    public void shouldRetrieveAllAuditOrderedByTimestampDesc() {

        final Iterable<Audit> all = repository.findAllByOrderByTimestampDesc();
        assertThat(all).size().isEqualTo(3);
        assertThat(all).extracting("timestamp").containsExactly(NOW_PLUS_60_MINS, NOW, NOW_MINUS_60_MINS);


    }

    private Audit createAudit(LocalDateTime timestamp) {
        Audit auditEntry = new Audit(
                java.util.UUID.randomUUID().toString(),
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


}
