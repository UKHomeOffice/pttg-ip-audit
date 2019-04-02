package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.digital.ho.pttg.application.ServiceConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class ArchivedResultTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        ServiceConfiguration serviceConfiguration = new ServiceConfiguration(objectMapper, 0, 0);
        objectMapper = ReflectionTestUtils.invokeMethod(serviceConfiguration, "initialiseObjectMapper", objectMapper);
    }

    @Test
    public void archivedResultMapper_singleResult_isSerialized() throws IOException {
        String expectedJson = IOUtils.toString(getClass().getResourceAsStream("/api/ArchivedResultTest/single-result.json"));

        Map<String, Integer> results = new HashMap<>();
        results.put("PASS", 1);
        String actualJson = objectMapper.writeValueAsString(new ArchivedResult(results));

        JSONAssert.assertEquals(expectedJson, actualJson, false);
    }

    @Test
    public void archivedResultMapper_multipleResults_isSerialized() throws IOException {
        String expectedJson = IOUtils.toString(getClass().getResourceAsStream("/api/ArchivedResultTest/multiple-results.json"));

        Map<String, Integer> results = new HashMap<>();
        results.put("PASS", 1);
        results.put("FAIL", 20);
        String actualJson = objectMapper.writeValueAsString(new ArchivedResult(results));

        JSONAssert.assertEquals(expectedJson, actualJson, false);
    }

    @Test
    public void archivedResultMapper_singleResult_isDeserialized() throws IOException {
        String actualJson = IOUtils.toString(getClass().getResourceAsStream("/api/ArchivedResultTest/single-result.json"));
        ArchivedResult actual = objectMapper.readValue(actualJson, ArchivedResult.class);

        Map<String, Integer> expectedResults = new HashMap<>();
        expectedResults.put("PASS", 1);
        ArchivedResult expected = new ArchivedResult(expectedResults);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void archivedResultMapper_multipleResults_isDeserialized() throws IOException {
        String actualJson = IOUtils.toString(getClass().getResourceAsStream("/api/ArchivedResultTest/multiple-results.json"));
        ArchivedResult actual = objectMapper.readValue(actualJson, ArchivedResult.class);

        Map<String, Integer> expectedResults = new HashMap<>();
        expectedResults.put("PASS", 1);
        expectedResults.put("FAIL", 20);
        ArchivedResult expected = new ArchivedResult(expectedResults);

        assertThat(actual).isEqualTo(expected);
    }

}
