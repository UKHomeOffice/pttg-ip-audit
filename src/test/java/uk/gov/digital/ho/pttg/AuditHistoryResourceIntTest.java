package uk.gov.digital.ho.pttg;

import com.jayway.jsonpath.JsonPath;
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
import uk.gov.digital.ho.pttg.api.AuditRecord;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_REQUEST;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@SqlGroup({
        @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "/sql/AuditHistoryServiceIntTest/before.sql"),
        @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "/sql/AuditHistoryServiceIntTest/after.sql")
})
public class AuditHistoryResourceIntTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void shouldRetrieveAuditHistory() throws JSONException {
        String url = "/history?toDate=2019-03-01&eventTypes=INCOME_PROVING_FINANCIAL_STATUS_REQUEST,INCOME_PROVING_FINANCIAL_STATUS_RESPONSE";
        String response = restTemplate.getForObject(url, String.class);

        String firstId = JsonPath.read(response, "$[0].id");
        String firstType = JsonPath.read(response, "$[0].ref");
        assertThat(firstId).isEqualTo("some corr id");
        assertThat(firstType).isEqualTo("INCOME_PROVING_FINANCIAL_STATUS_REQUEST");

        String secondId = JsonPath.read(response, "$[1].id");
        String secondType = JsonPath.read(response, "$[1].ref");
        assertThat(secondId).isEqualTo("some corr id");
        assertThat(secondType).isEqualTo("INCOME_PROVING_FINANCIAL_STATUS_RESPONSE");

    }

}
