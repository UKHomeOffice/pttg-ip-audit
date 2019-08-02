package uk.gov.digital.ho.pttg.alert;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import uk.gov.digital.ho.pttg.AuditEventType;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-alertintegrationtest.properties")
public class AlertingIntegrationTest {

    @MockBean Clock clock;

    private static final String AUDIT_ENDPOINT = "/audit";
    private MultiValueMap<String, String> headers;
    private static final LocalDateTime SOME_TIMESTAMP = LocalDateTime.of(2017, 9, 11, 14, 50);

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(options().port(8084));

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${alert.individual.usage.daily.threshold}")
    private int dailyUsageThreshold;

    @Value("${alert.match.failure.threshold}")
    private int matchFailureThreshold;

    @Before
    public void setup() {
        stubFor(post(urlPathMatching("/api/events"))
            .willReturn(aResponse()
                .withStatus(200)));

        headers = new HttpHeaders();
        headers.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);

        when(clock.instant()).thenReturn(Instant.parse("2017-09-11T18:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    }

    @Test
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "/sql/AlertingIntegrationTest/before-below-threshold.sql"),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "/sql/AlertingIntegrationTest/after.sql")
    })
    @Transactional
    public void shouldNotAlertWhenManyRequestsBelowThreshold() throws Exception {
        makeRequests();

        verify(
            0,
            postRequestedFor(
                urlEqualTo("/api/events")).
                withRequestBody(containing("Proving Things, Income Proving, Reasonable Usage"))
        );
    }

    @Test
    @SqlGroup({
            @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "/sql/AlertingIntegrationTest/before-at-threshold.sql"),
            @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "/sql/AlertingIntegrationTest/after.sql")
    })
    @Transactional
    public void shouldAlertWhenTooManyRequests() throws Exception {
        makeRequests();

        verify(
            postRequestedFor(
                urlEqualTo("/api/events")).
                withHeader("Content-Type", equalTo("application/json")).
                withHeader("Authorization", equalTo("Bearer test-sysdig-secret")).
                withRequestBody(containing("Proving Things, Income Proving, Excessive Usage"))
        );
    }

    private void makeRequests() {
        UUID eventId = UUID.randomUUID();
        restTemplate.exchange(AUDIT_ENDPOINT, POST, createRequestAuditEntity(eventId), Void.class);
        restTemplate.exchange(AUDIT_ENDPOINT, POST, createResponseAuditEntity(eventId), Void.class);
    }

    private HttpEntity<String> createRequestAuditEntity(UUID eventId) {

        JSONObject requestBodyJson = new JSONObject()
                .put("eventId", eventId)
                .put("timestamp", SOME_TIMESTAMP)
                .put("sessionId", "some session id")
                .put("correlationId", "some correlation id")
                .put("userId", "bobby.bag@digital.homeoffice.gov.uk")
                .put("deploymentName", "some deployment")
                .put("deploymentNamespace", "some namespace")
                .put("eventType", AuditEventType.INCOME_PROVING_INCOME_CHECK_REQUEST)
                .put("data", "{}")
                ;

        return new HttpEntity<>(requestBodyJson.toString(), headers);
    }

    private HttpEntity<String> createResponseAuditEntity(UUID eventId) {

        JSONObject requestBodyJson = new JSONObject()
                .put("eventId", eventId)
                .put("timestamp", SOME_TIMESTAMP)
                .put("sessionId", "some session id")
                .put("correlationId", "some correlation id")
                .put("userId", "bobby.bag@digital.homeoffice.gov.uk")
                .put("deploymentName", "some deployment")
                .put("deploymentNamespace", "some namespace")
                .put("eventType", INCOME_PROVING_FINANCIAL_STATUS_RESPONSE)
                .put("data", "{}")
                ;

        return new HttpEntity<>(requestBodyJson.toString(), headers);
    }

}
