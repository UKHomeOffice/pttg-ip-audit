package uk.gov.digital.ho.pttg;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import org.skyscreamer.jsonassert.JSONAssert;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class AuditResourceIntTest {

    static final String SESSION_ID = "sessionID";
    static final String DEPLOYMENT = "deployment";
    static final String NAMESPACE = "env";

    @Autowired
    private AuditRepository repository;
    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        repository.save(new Audit(
                "3a22c723-ea0f-4962-b97b-f35dce3284b2",
                LocalDateTime.parse("2017-09-11T14:45:48.094"),
                SESSION_ID,
                "3a22c723-ea0f-4962-b97b-f35dce3284b2",
                "bobby.bag@digital.homeoffice.gov.uk",
                DEPLOYMENT,
                NAMESPACE,
                AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_REQUEST,
                "{\n" +
                        "      \"forename\": \"Antonio\",\n" +
                        "      \"method\": \"get-financial-status\",\n" +
                        "      \"dependants\": 0,\n" +
                        "      \"surname\": \"Gramsci\",\n" +
                        "      \"applicationRaisedDate\": \"2017-06-01\",\n" +
                        "      \"dateOfBirth\": \"1891-01-22\",\n" +
                        "      \"nino\": \"NE112233C\"\n" +
                        "    }"
        ));
        repository.save(new Audit(
                "3a22c723-ea0f-4962-b97b-f35dce3284b2",
                LocalDateTime.parse("2017-09-11T14:45:55.033"),
                SESSION_ID,
                "3a22c723-ea0f-4962-b97b-f35dce3284b2",
                "bobby.bag@digital.homeoffice.gov.uk",
                DEPLOYMENT,
                NAMESPACE,
                AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
                "{\n" +
                        "      \"method\": \"get-financial-status\",\n" +
                        "      \"response\": {\n" +
                        "        \"status\": {\n" +
                        "          \"code\": \"100\",\n" +
                        "          \"message\": \"OK\"\n" +
                        "        },\n" +
                        "        \"individual\": {\n" +
                        "          \"title\": \"\",\n" +
                        "          \"forename\": \"Antonio\",\n" +
                        "          \"surname\": \"Gramsci\",\n" +
                        "          \"nino\": \"NE112233C\"\n" +
                        "        },\n" +
                        "        \"categoryCheck\": {\n" +
                        "          \"category\": \"A\",\n" +
                        "          \"passed\": false,\n" +
                        "          \"applicationRaisedDate\": \"2017-06-01\",\n" +
                        "          \"assessmentStartDate\": \"2016-12-01\",\n" +
                        "          \"failureReason\": \"MONTHLY_VALUE_BELOW_THRESHOLD\",\n" +
                        "          \"threshold\": 1550,\n" +
                        "          \"employers\": [\n" +
                        "            \"MCDONALDS\"\n" +
                        "          ]\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }"
        ));
    }



    @Test
    public void shouldRetrieveAllAudit() throws IOException, JSONException {
        String auditRecords = restTemplate.getForObject("/audit", String.class);
        String expectedRecords = IOUtils.toString(getClass().getResourceAsStream("/expected.json"));

        JSONAssert.assertEquals(auditRecords, expectedRecords, false);
    }

}
