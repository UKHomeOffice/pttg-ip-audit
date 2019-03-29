package uk.gov.digital.ho.pttg;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.pttg.alert.CountByUser;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_REQUEST;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
//@ActiveProfiles("postgres,postgresDocker")
public class AuditEntryJpaRepositoryTest {

    private static final String SESSION_ID = "sessionID";
    private static final String DEPLOYMENT = "deployment";
    private static final String USER_ID = "me";
    private static final String UUID = "uuid";
    private static final String NAMESPACE = "env";
    private static final String DETAIL = "{}";
    private static final LocalDateTime NOW = LocalDateTime.of(2017, 12, 8, 12, 0);
    private static final LocalDateTime NOW_MINUS_60_MINS = NOW.minusMinutes(60);
    private static final LocalDateTime NOW_PLUS_60_MINS = NOW.plusMinutes(60);
    private static final LocalDateTime TWO_DAYS_AGO = NOW.minusDays(2);
    private static final LocalDateTime YESTERDAY = NOW.minusDays(1);
    private static final LocalDateTime TOMORROW = NOW.plusDays(1);
    private static final LocalDateTime DAY_AFTER_TOMORROW = NOW.plusDays(2);

    @Autowired
    private AuditEntryJpaRepository repository;

    @Before
    public void setup() {
        repository.save(createAudit(TWO_DAYS_AGO));
        repository.save(createAudit(YESTERDAY));
        repository.save(createAudit(NOW_MINUS_60_MINS));
        repository.save(createAudit(NOW));
        repository.save(createAudit(NOW_PLUS_60_MINS));
        repository.save(createAudit(TOMORROW));
        repository.save(createAudit(DAY_AFTER_TOMORROW));
    }

    @Test
    public void shouldRetrieveAllAuditData() {

        final Iterable<AuditEntry> all = repository.findAll();
        assertThat(all).size().isEqualTo(7);
    }

    @Test
    public void shouldGetCountOfEntriesBetweenDates() {

        Long numberOfEntries = repository.countEntriesBetweenDates(YESTERDAY.withHour(0).withMinute(0),
                                                                    TOMORROW.withHour(23).withMinute(59),
                                                                    INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
                                                                    NAMESPACE);

        assertThat(numberOfEntries).isEqualTo(5);
    }

    @Test
    public void shouldGetEntriesBetweenDates() {

        repository.save(createAudit(NOW.minusMinutes(10))); // out of sequence addition to the table

        List<AuditEntry> auditEntries = repository.getEntriesBetweenDates(YESTERDAY.withHour(0).withMinute(0),
                                                                            TOMORROW.withHour(23).withMinute(59),
                                                                            INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
                                                                            NAMESPACE);

        assertThat(auditEntries)
                .extracting("timestamp")
                .containsExactly(
                        YESTERDAY,
                        NOW_MINUS_60_MINS,
                        NOW.minusMinutes(10),
                        NOW,
                        NOW_PLUS_60_MINS,
                        TOMORROW
                        );
    }

    @Test
    public void shouldGetEntriesBetweenDatesGroupedByUser() {

        String anotherUserId = "another user id";

        repository.save(createAudit(TWO_DAYS_AGO, anotherUserId));
        repository.save(createAudit(NOW, anotherUserId));
        repository.save(createAudit(DAY_AFTER_TOMORROW, anotherUserId));

        List<CountByUser> auditEntries = repository.countEntriesBetweenDatesGroupedByUser(YESTERDAY.withHour(0).withMinute(0),
                                                                                            TOMORROW.withHour(23).withMinute(59),
                                                                                            INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
                                                                                            NAMESPACE);

        assertThat(auditEntries).hasSize(2);

        assertThat(auditEntries)
                .extracting("userId")
                .containsExactly("another user id", "me");

        assertThat(auditEntries)
                .extracting("count")
                .containsExactly(1L, 5L);
    }

    @Test
    public void shouldRetrieveAllAuditDataLimitedByPagination() {

        Pageable pagination = PageRequest.of(0, 1);

        final Iterable<AuditEntry> all = repository.findAllByOrderByTimestampDesc(pagination);

        assertThat(all).size().isEqualTo(1);
        assertThat(all)
                .extracting("timestamp")
                .containsExactly(DAY_AFTER_TOMORROW);
    }

    @Test
    public void shouldRetrieveAllAuditOrderedByTimestampDesc() {

        final Iterable<AuditEntry> all = repository.findAllByOrderByTimestampDesc(null);
        assertThat(all).size().isEqualTo(7);
        assertThat(all)
                .extracting("timestamp")
                .containsExactly(
                        DAY_AFTER_TOMORROW,
                        TOMORROW,
                                    NOW_PLUS_60_MINS,
                                    NOW,
                                    NOW_MINUS_60_MINS,
                                    YESTERDAY,
                                    TWO_DAYS_AGO);
    }

    @Test
    public void findAuditHistory_filtersByDate() {
        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
        final Iterable<AuditEntry> all = repository.findAuditHistory(YESTERDAY, eventTypes);

        assertThat(all)
                .extracting("timestamp")
                .containsExactly(TWO_DAYS_AGO, YESTERDAY);

    }

    @Test
    public void findAuditHistory_ordersByDateAscending() {
        repository.save(createAudit(TWO_DAYS_AGO));
        repository.save(createAudit(NOW));
        repository.save(createAudit(TWO_DAYS_AGO));

        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
        final Iterable<AuditEntry> all = repository.findAuditHistory(YESTERDAY, eventTypes);

        assertThat(all)
                .extracting("timestamp")
                .containsExactly(TWO_DAYS_AGO, TWO_DAYS_AGO, TWO_DAYS_AGO, YESTERDAY);
    }

    @Test
    public void findAuditHistory_filtersByEventType() {
        List<AuditEventType> eventTypes = Arrays.asList(INCOME_PROVING_FINANCIAL_STATUS_REQUEST);
        final Iterable<AuditEntry> all = repository.findAuditHistory(YESTERDAY, eventTypes);

        assertThat(all).size().isEqualTo(0);
    }

    /**
     * As this test involves some Postgres specific syntax for the JSONB column, the intention was to run this test
     * against an in memory postgres database.  However, I was unable to get such a database working consistently in
     * conjunction with Spring Boot.
     *
     * This test was run manually against postgres 9.6 running in a docker container, and it passed there, so we do at
     * least know the test passes against a real postgres db.
     *
     * To run this test:
     * - add the class level annotation @ActiveProfiles("postgres,postgresDocker")
     * - run a docker container with the following command (replacing with the correct version of postgres):
     *     docker run  --name pg-docker -e POSTGRES_PASSWORD=docker -d -p 5432:5432 postgres:9.6.12
     * - remove the @Ignore annotation from the test
     * You should now be able to run the test against the docker container.
     */
    @Ignore
    @Test
    public void shouldCountByNino_basic() {
        repository.save(createAudit(LocalDateTime.now().plusDays(10), "some_user", "{\"nino\": \"some_nino\"}"));
        final Long count = repository.countNinosAfterDate(LocalDateTime.now().plusDays(9), "some_nino");
        assertThat(count).isEqualTo(1);
    }

    @Ignore
    @Test
    public void shouldCountByNino_multipleNinos() {
        repository.save(createAudit(LocalDateTime.now().plusDays(10), "some_user", "{\"nino\": \"some_nino_2\"}"));
        repository.save(createAudit(LocalDateTime.now().plusDays(10), "some_user", "{\"nino\": \"some_nino_3\"}"));
        final Long count = repository.countNinosAfterDate(LocalDateTime.now().plusDays(9), "some_nino_2");
        assertThat(count).isEqualTo(1);
    }

    @Ignore
    @Test
    public void shouldCountByNino_dateFilterIsApplied() {
        repository.save(createAudit(LocalDateTime.now().plusDays(9), "some_user", "{\"nino\": \"some_nino_4\"}"));
        repository.save(createAudit(LocalDateTime.now().plusDays(10), "some_user", "{\"nino\": \"some_nino_4\"}"));
        final Long count = repository.countNinosAfterDate(LocalDateTime.now().plusDays(9), "some_nino_4");
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void shouldDeleteSingleCorrelationId() {
        repository.deleteAllCorrelationIds(Arrays.asList(UUID));
        final Iterable<AuditEntry> all = repository.findAll();
        assertThat(all).size().isEqualTo(0);
    }

    @Test
    public void shouldDeleteMultipleCorrelationId() {
        repository.save(createAuditWithCorrelationId(LocalDateTime.now(), "some_user", "corr id 2"));
        repository.deleteAllCorrelationIds(Arrays.asList(UUID, "corr id 2"));
        final Iterable<AuditEntry> all = repository.findAll();
        assertThat(all).size().isEqualTo(0);
    }

    @Test
    public void shouldNotDeleteMissingCorrelationId() {
        repository.deleteAllCorrelationIds(Arrays.asList("does_not_exist"));
        final Iterable<AuditEntry> all = repository.findAll();
        assertThat(all).size().isEqualTo(7);
    }

    private AuditEntry createAudit(LocalDateTime timestamp) {
        return createAudit(timestamp, USER_ID);
    }

    private AuditEntry createAudit(LocalDateTime timestamp, String userId) {
        return createAudit(timestamp, userId, DETAIL);
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

    private AuditEntry createAuditWithCorrelationId(LocalDateTime timestamp, String userId, String correlationId) {
        return new AuditEntry(
                randomUUID().toString(),
                timestamp,
                SESSION_ID,
                correlationId,
                userId,
                DEPLOYMENT,
                NAMESPACE,
                INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
                DETAIL
        );
    }

}
