package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@SqlGroup({
        @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "/sql/AuditHistoryCorrelationIdIntTest/before.sql"),
        @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "/sql/AuditHistoryCorrelationIdIntTest/after.sql")
})
public class AuditHistoryResourceCorrelationIdIntTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldReturnCorrelationIdsForRequestedEventTypes() throws IOException {
        String url = "/correlationIds?eventTypes=INCOME_PROVING_FINANCIAL_STATUS_REQUEST,INCOME_PROVING_FINANCIAL_STATUS_RESPONSE";
        String response = restTemplate.getForObject(url, String.class);

        List<String> correlationIds = objectMapper.readValue(response, List.class);
        assertThat(correlationIds).containsExactlyInAnyOrder("correlationID1", "correlationID3");
    }

    @Test
    public void shouldReturnCorrelationIdsForRequesteEventTypesBeforeDate() throws IOException {
        String url = "/correlationIds?eventTypes=INCOME_PROVING_FINANCIAL_STATUS_REQUEST,INCOME_PROVING_FINANCIAL_STATUS_RESPONSE";
        url += "&toDate=2017-09-11";
        String response = restTemplate.getForObject(url, String.class);

        List<String> correlationIds = objectMapper.readValue(response, List.class);
        assertThat(correlationIds).containsExactly("correlationID1");
    }
}
