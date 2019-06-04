package uk.gov.digital.ho.pttg.api;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.pttg.ArchiveService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsNot.not;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@SqlGroup({
        @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "/sql/ArchiveResourceIntTest/before.sql"),
        @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "/sql/ArchiveResourceIntTest/after.sql")
})
public class ArchiveResourceLoggingIntTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ArchiveService archiveService;

    @Rule
    public final OutputCapture outputCapture = new OutputCapture();

    @Test
    public void shouldNotLogNino() {
        ArchiveRequest archivedRequest = new ArchiveRequest("PASS", LocalDate.now(), asList("any-correlation-id"), "AA123456A");
        String yesterday = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now().minusDays(1));

        restTemplate.postForEntity(String.format("/archive/%s", yesterday), archivedRequest, Void.class);

        outputCapture.expect(not(containsString("AA123456A")));
    }
}
