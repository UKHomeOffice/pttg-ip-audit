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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import uk.gov.digital.ho.pttg.AuditEventType;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration
@TestPropertySource(locations = "classpath:application-alertintegrationtest.properties")
@Transactional
public class AlertingIntegrationTest {

    private static final String auditEndpoint = "/audit";
    private MultiValueMap<String, String> headers;

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
    }

    @Test
    public void shouldNotAlertWhenManyRequestsBelowThreshold() throws Exception {
        makeRequests(dailyUsageThreshold);

        verify(
            0,
            postRequestedFor(
                urlEqualTo("/api/events")).
                withRequestBody(containing("Proving Things, Income Proving, Excessive Usage"))
        );
    }

    @Test
    public void shouldAlertWhenTooManyRequests() throws Exception {
        makeRequests(dailyUsageThreshold + 1);

        verify(
            postRequestedFor(
                urlEqualTo("/api/events")).
                withHeader("Content-Type", equalTo("application/json")).
                withHeader("Authorization", equalTo("Bearer test-sysdig-secret")).
                withRequestBody(containing("Proving Things, Income Proving, Excessive Usage"))
        );
    }

    private void makeRequests(int count) {

        for (int i = 0; i < count; i++) {

            UUID eventId = UUID.randomUUID();

            restTemplate.exchange(auditEndpoint, POST, createRequestAuditEntity(eventId), Void.class);
            restTemplate.exchange(auditEndpoint, POST, createResponseAuditEntity(eventId), Void.class);
        }
    }

    private HttpEntity<String> createRequestAuditEntity(UUID eventId) {

        LocalDateTime someTimestamp = LocalDateTime.now();

        JSONObject requestBodyJson = new JSONObject()
                .put("eventId", eventId)
                .put("timestamp", someTimestamp)
                .put("sessionId", "some session id")
                .put("correlationId", "some correlation id")
                .put("userId", "some user id")
                .put("deploymentName", "some deployment name")
                .put("deploymentNamespace", "local")
                .put("eventType", AuditEventType.INCOME_PROVING_INCOME_CHECK_REQUEST)
                .put("data", "some data")
                ;

        return new HttpEntity<>(requestBodyJson.toString(), headers);
    }

    private HttpEntity<String> createResponseAuditEntity(UUID eventId) {

        LocalDateTime someTimestamp = LocalDateTime.now();

        JSONObject requestBodyJson = new JSONObject()
                .put("eventId", eventId)
                .put("timestamp", someTimestamp)
                .put("sessionId", "some session id")
                .put("correlationId", "some correlation id")
                .put("userId", "some user id")
                .put("deploymentName", "some deployment name")
                .put("deploymentNamespace", "local")
                .put("eventType", INCOME_PROVING_FINANCIAL_STATUS_RESPONSE)
                .put("data", "some data")
                ;

        return new HttpEntity<>(requestBodyJson.toString(), headers);
    }

}
