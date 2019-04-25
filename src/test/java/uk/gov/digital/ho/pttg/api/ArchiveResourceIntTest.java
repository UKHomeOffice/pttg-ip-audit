package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.digital.ho.pttg.application.ServiceConfiguration;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static java.util.Arrays.asList;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@SqlGroup({
        @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "/sql/ArchiveResourceIntTest/before.sql"),
        @Sql(executionPhase = AFTER_TEST_METHOD, scripts = "/sql/ArchiveResourceIntTest/after.sql")
})
public class ArchiveResourceIntTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldReturnArchivedResults() throws JsonProcessingException {
        List<ArchivedResult> expected = asList(
                new ArchivedResult(ImmutableMap.of("PASS", 5, "FAIL", 3)),
                new ArchivedResult(ImmutableMap.of("ERROR", 2, "NOTFOUND", 99))
        );

        String archivedResults = restTemplate.getForObject("/archive?fromDate={fromDate}&toDate={toDate}", String.class, ImmutableMap.of(
                "fromDate", LocalDate.of(2018, Month.SEPTEMBER, 1),
                "toDate", LocalDate.of(2018, Month.SEPTEMBER, 30)
        ));
        String expectedRecords = objectMapper.writeValueAsString(expected);

        JSONAssert.assertEquals(archivedResults, expectedRecords, false);
    }
}