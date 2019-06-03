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
import java.time.DateTimeException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@SqlGroup({
        @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "/sql/AuditHistoryRecordsIntTest/before.sql"),
        @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "/sql/AuditHistoryRecordsIntTest/after.sql")
})
public class AuditHistoryResourceRecordsIntTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldRetrieveRecords() throws IOException {
        String url = "/historyByCorrelationId?correlationId=correlation-id-1&eventTypes=INCOME_PROVING_FINANCIAL_STATUS_REQUEST,INCOME_PROVING_FINANCIAL_STATUS_RESPONSE";
        String response = restTemplate.getForObject(url, String.class);

        assertThat(objectMapper.readValue(response, List.class))
                .hasSize(2);

        String firstCorrelationId = JsonPath.read(response, "$.[0].id");
        String secondCorrelationId = JsonPath.read(response, "$.[1].id");
        assertThat(firstCorrelationId)
                .isEqualTo(secondCorrelationId)
                .isEqualTo("correlation-id-1");
    }
}
