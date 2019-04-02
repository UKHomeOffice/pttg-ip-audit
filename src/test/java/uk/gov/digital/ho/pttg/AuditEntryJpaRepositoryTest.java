package uk.gov.digital.ho.pttg;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.pttg.alert.CountByUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.AuditEventType.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
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

    @Test
    public void findArchivedResults_noResults_nothing() {
        List<AuditEntry> results = repository.findArchivedResults(LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay());

        assertThat(results).isEmpty();
    }

    @Test
    public void findArchivedResults_singleResult_isReturned() {
        AuditEntry archivedResult = createArchivedResult(LocalDate.now());
        repository.save(archivedResult);

        List<AuditEntry> results = repository.findArchivedResults(LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay());

        assertThat(results).containsExactly(archivedResult);
    }

    @Test
    public void findArchivedResults_multipleResults_correctDatesReturned() {
        AuditEntry archivedResult1 = createArchivedResult(LocalDate.now());
        AuditEntry archivedResult2 = createArchivedResult(LocalDate.now().minusDays(1));
        AuditEntry archivedResult3 = createArchivedResult(LocalDate.now().minusDays(2));
        repository.save(archivedResult1);
        repository.save(archivedResult2);
        repository.save(archivedResult3);

        List<AuditEntry> results = repository.findArchivedResults(LocalDate.now().minusDays(1).atStartOfDay(), LocalDate.now().atStartOfDay());

        assertThat(results).containsExactly(archivedResult1, archivedResult2);
    }

    private AuditEntry createAudit(LocalDateTime timestamp) {
        return createAudit(timestamp, USER_ID);
    }

    private AuditEntry createAudit(LocalDateTime timestamp, String userId) {
        return new AuditEntry(
                randomUUID().toString(),
                timestamp,
                SESSION_ID,
                UUID,
                userId,
                DEPLOYMENT,
                NAMESPACE,
                INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
                DETAIL
        );
    }

    private AuditEntry createArchivedResult(LocalDate date) {
        return new AuditEntry(
                randomUUID().toString(),
                date.atStartOfDay(),
                "",
                randomUUID().toString(),
                "Audit Service",
                "",
                "",
                ARCHIVED_RESULTS,
                "{\"results\": {\"PASS\": 1}}"
        );
    }

}
