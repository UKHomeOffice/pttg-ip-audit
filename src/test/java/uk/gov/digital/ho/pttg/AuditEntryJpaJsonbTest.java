package uk.gov.digital.ho.pttg;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE;


/**
 * As these tests involve some Postgres specific syntax for the JSONB column, the intention was to run these tests
 * against an in memory postgres database.  However, I was unable to get such a database working consistently in
 * conjunction with Spring Boot.
 *
 * The tests were run manually against postgres 9.6 running in a docker container, and it passed there, so we do at
 * least know the test passes against a real postgres db.  The test has been left here in an ignored state so that
 * it can be resurrected should we experience issues with the JSONB column.
 *
 * To run this test:
 * - run a docker container with the following command (replacing with the correct version of postgres):
 *     docker run  --name pg-docker -e POSTGRES_PASSWORD=docker -d -p 5432:5432 postgres:9.6.12
 * - remove the @Ignore annotation from the class
 * - run this test class from your IDE
 */
//@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@ActiveProfiles("postgres,postgresDocker")
public class AuditEntryJpaJsonbTest {

    private static final String SESSION_ID = "sessionID";
    private static final String DEPLOYMENT = "deployment";
    private static final String SOME_USER = "some_user";
    private static final String UUID = "uuid";
    private static final String NAMESPACE = "env";

    @Autowired
    private AuditEntryJpaRepository repository;

    @Test
    public void shouldCountByNino_basic() {
        repository.save(createAudit(LocalDateTime.now().plusDays(10), SOME_USER, "{\"nino\": \"some_nino\"}"));
        final Long count = repository.countNinosAfterDate(LocalDateTime.now().plusDays(9), "some_nino");
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void shouldCountByNino_multipleNinos() {
        repository.save(createAudit(LocalDateTime.now().plusDays(10), SOME_USER, "{\"nino\": \"some_nino_2\"}"));
        repository.save(createAudit(LocalDateTime.now().plusDays(10), SOME_USER, "{\"nino\": \"some_nino_3\"}"));
        final Long count = repository.countNinosAfterDate(LocalDateTime.now().plusDays(9), "some_nino_2");
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void shouldCountByNino_dateFilterIsApplied() {
        repository.save(createAudit(LocalDateTime.now().plusDays(9), SOME_USER, "{\"nino\": \"some_nino_4\"}"));
        repository.save(createAudit(LocalDateTime.now().plusDays(10), SOME_USER, "{\"nino\": \"some_nino_4\"}"));
        final Long count = repository.countNinosAfterDate(LocalDateTime.now().plusDays(9), "some_nino_4");
        assertThat(count).isEqualTo(1);
    }

    private AuditEntry createAudit(LocalDateTime timestamp, String userId, String detail) {
        return new AuditEntry(
                randomUUID().toString(),
                timestamp,
                SESSION_ID,
                UUID,
                userId,
                DEPLOYMENT,
                NAMESPACE,
                INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
                detail
        );
    }

}
