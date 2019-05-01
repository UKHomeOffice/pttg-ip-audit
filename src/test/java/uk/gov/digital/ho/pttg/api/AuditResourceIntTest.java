package uk.gov.digital.ho.pttg.api;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MultiValueMap;
import uk.gov.digital.ho.pttg.AuditEntryJpaRepository;
import uk.gov.digital.ho.pttg.AuditEventType;
import uk.gov.digital.ho.pttg.ServiceRunner;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@SqlGroup({
        @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "/sql/AuditEntryResourceIntTest/before.sql"),
        @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "/sql/AuditEntryResourceIntTest/after.sql")
})
public class AuditResourceIntTest {

    private MultiValueMap<String, String> headers;
    private static final LocalDateTime SOME_TIMESTAMP = LocalDateTime.of(2017, 9, 11, 14, 50);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AuditEntryJpaRepository auditEntryJpaRepository;

    @Before
    public void setup(){
        headers = new HttpHeaders();
        headers.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
    }

    @Test
    public void shouldRetrieveAllAudit() throws IOException, JSONException {
        String auditRecords = restTemplate.getForObject("/audit", String.class);
        String expectedRecords = IOUtils.toString(getClass().getResourceAsStream("/expected.json"));

        JSONAssert.assertEquals(auditRecords, expectedRecords, false);
    }

    @Test
    public void shouldAddAuditEvent() {
        long beforeCount = auditEntryJpaRepository.count();

        ResponseEntity responseEntity = restTemplate.exchange("/audit", POST, createRequestAuditEntity("new uuid"), Void.class);
        long afterCount = auditEntryJpaRepository.count();

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(afterCount).isEqualTo(beforeCount + 1l);

    }

    @Test
    public void shouldNotAddDuplicateAuditEvent() {

        long beforeCount = auditEntryJpaRepository.count();

        ResponseEntity responseEntity = restTemplate.exchange("/audit", POST, createRequestAuditEntity("some uuid"), Void.class);

        long afterCount = auditEntryJpaRepository.count();

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(afterCount).isEqualTo(beforeCount);


    }

    private HttpEntity<String> createRequestAuditEntity(String eventUuid) {

        JSONObject requestBodyJson = new JSONObject()
                .put("eventId", eventUuid)
                .put("timestamp", SOME_TIMESTAMP)
                .put("sessionId", "some session id")
                .put("correlationId", "some correlation id")
                .put("userId", "bobby.bag@digital.homeoffice.gov.uk")
                .put("deploymentName", "some deployment")
                .put("deploymentNamespace", "some namespace")
                .put("eventType", AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_REQUEST)
                .put("data", "{}");

        return new HttpEntity<>(requestBodyJson.toString(), headers);
    }

}
