package uk.gov.digital.ho.pttg;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.pttg.api.ArchiveRequest;

import java.time.LocalDate;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.not;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static uk.gov.digital.ho.pttg.AuditEventType.ARCHIVED_RESULTS;

/**
 * These tests use AuditEntryJpaRepositoryStubCountNinosAfterDate which is activated in profile stubAuditRepoCountNinosAfterDate.
 *
 * This bean stubs the countNinosAfterDate method but delegates everything else to the real repository so that we can test
 * against an Hsql in memory database.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@SqlGroup({
        @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "/sql/ArchiveServiceIntTest/before.sql"),
        @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "/sql/ArchiveServiceIntTest/after.sql")
})
@ActiveProfiles("stubAuditRepoCountNinosAfterDate")
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
                .filteredOn("type", not(ARCHIVED_RESULTS))
                .extracting("correlationId")
                .containsExactly("corr id 3", "corr id 4", "corr id 5", "corr id 6");
    }

    @Test
    public void archiveResult_noExistingArchive_archiveIsCreated() {
        String expectedArchivedResult = "{\"results\": {\"PASS\":1}}";
        ArchiveRequest archiveRequest = getArchiveRequest();

        restTemplate.postForLocation("/archive/2017-09-01", archiveRequest);

        Iterable<AuditEntry> all = repository.findAll();
        assertThat(all)
                .filteredOn("type", ARCHIVED_RESULTS)
                .filteredOn("correlationId", not("corr id 50"))
                .extracting("detail")
                .hasOnlyOneElementSatisfying(actual -> JSONAssert.assertEquals(expectedArchivedResult, (String)actual, false));
    }

    @Test
    public void archiveResult_existingArchive_archiveIsUpdated() {
        String expectedArchivedResult = "{\"results\": {\"PASS\":2,\"FAIL\":2}}";
        ArchiveRequest archiveRequest = getArchiveRequest();

        restTemplate.postForLocation("/archive/2017-09-02", archiveRequest);

        Iterable<AuditEntry> all = repository.findAll();
        assertThat(all)
                .filteredOn("type", ARCHIVED_RESULTS)
                .filteredOn("correlationId", "corr id 50")
                .extracting("detail")
                .hasOnlyOneElementSatisfying(actual -> JSONAssert.assertEquals(expectedArchivedResult, (String)actual, false));

    }

    @Test
    public void archiveResult_ninoHasRequestsPostArchive_nothingIsChanged() {
        ArchiveRequest archiveRequest = getArchiveRequestNinoWithDataAfterLastArchiveDate();
        restTemplate.postForLocation("/archive/2017-09-03", archiveRequest);

        Iterable<AuditEntry> all = repository.findAll();
        assertThat(all)
                .filteredOn("type", ARCHIVED_RESULTS)
                .filteredOn("timestamp", LocalDate.of(2017, 9, 3).atStartOfDay())
                .hasSize(0);
    }

    private ArchiveRequest getArchiveRequest() {
        return ArchiveRequest.builder()
                .lastArchiveDate(LocalDate.of(2017, 9, 13))
                .correlationIds(asList("corr id 1", "corr id 2"))
                .result("PASS")
                .nino("AA123456A")
                .build();
    }

    private ArchiveRequest getArchiveRequestNinoWithDataAfterLastArchiveDate() {
        return ArchiveRequest.builder()
                .lastArchiveDate(LocalDate.of(2017, 9, 30))
                .correlationIds(asList("corr id 1", "corr id 2"))
                .result("PASS")
                .nino("AA123456A")
                .build();
    }
}
