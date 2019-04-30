package uk.gov.digital.ho.pttg.api;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@SqlGroup({
        @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "/sql/AuditEntryResourceIntTest/before.sql"),
        @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "/sql/AuditEntryResourceIntTest/after.sql")
})
public class AuditResourceIntTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AuditEntryJpaRepository auditEntryJpaRepository;

    @Test
    public void shouldRetrieveAllAudit() throws IOException, JSONException {
        String auditRecords = restTemplate.getForObject("/audit", String.class);
        String expectedRecords = IOUtils.toString(getClass().getResourceAsStream("/expected.json"));

        JSONAssert.assertEquals(auditRecords, expectedRecords, false);
    }
}
