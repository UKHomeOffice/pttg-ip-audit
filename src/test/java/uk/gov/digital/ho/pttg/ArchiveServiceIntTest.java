package uk.gov.digital.ho.pttg;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.pttg.api.ArchiveRequest;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static uk.gov.digital.ho.pttg.AuditEventType.ARCHIVED_RESULTS;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@SqlGroup({
        @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "/sql/AuditEntryResourceIntTest/before.sql"),
        @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "/sql/AuditEntryResourceIntTest/after.sql")
})
public class ArchiveServiceIntTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AuditEntryJpaRepository repository;

    @Test
    public void archiveResult_eventsAreRemoved() {
        ArchiveRequest archiveRequest = getArchiveRequest();
        restTemplate.postForLocation("/archive/2017-09-01", archiveRequest);

        Iterable<AuditEntry> all = repository.findAll();

        assertThat(all)
                .extracting("correlation_id")
                .containsExactly("corr id 3", "corr id 4", "corr id 5", "corr id 6");
    }

    @Test
    public void archiveResult_noExistingArchive_archiveIsCreated() {
        ArchiveRequest archiveRequest = getArchiveRequest();
        restTemplate.postForLocation("/archive/2017-09-01", archiveRequest);

        Optional<AuditEntry> archive = findfirstAuditEntry(ARCHIVED_RESULTS);

        assertThat(archive.isPresent()).isTrue();
        JSONAssert.assertEquals(archive.get().getDetail(), "{\"results\": {\"PASS\":1}}", false);
    }

    @Test
    public void archiveResult_existingArchive_archiveIsUpdated() {
        ArchiveRequest archiveRequest = getArchiveRequest();
        restTemplate.postForLocation("/archive/2017-09-02", archiveRequest);

        Optional<AuditEntry> archive = findfirstAuditEntry(ARCHIVED_RESULTS);

        assertThat(archive.isPresent()).isTrue();
        JSONAssert.assertEquals(archive.get().getDetail(), "{\"results\": {\"PASS\":2,\"FAIL\":2}}", false);
    }

    @Test
    public void archiveResult_ninoHasRequestsPostArchive_nothingIsChanged() {
        ArchiveRequest archiveRequest = getArchiveRequest();
        restTemplate.postForLocation("/archive/2017-09-01", archiveRequest);

        Iterable<AuditEntry> all = repository.findAll();

        assertThat(all).size().isEqualTo(7);
        Optional<AuditEntry> archive = findfirstAuditEntry(ARCHIVED_RESULTS);
        assertThat(archive.isPresent()).isFalse();
    }

    private Optional<AuditEntry> findfirstAuditEntry(AuditEventType auditEventType) {
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .filter(auditEntry -> auditEntry.getType().equals(auditEventType))
                .findFirst();
    }

    private ArchiveRequest getArchiveRequest() {
        return ArchiveRequest.builder()
                .lastArchiveDate(LocalDate.of(2017, 9, 13))
                .eventIds(asList("corr id 1", "corr id 2"))
                .result("PASS")
                .build();
    }
}
