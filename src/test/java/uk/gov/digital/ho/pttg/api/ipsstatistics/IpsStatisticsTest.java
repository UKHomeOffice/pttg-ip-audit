package uk.gov.digital.ho.pttg.api.ipsstatistics;

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
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class IpsStatisticsTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        ServiceConfiguration serviceConfiguration = new ServiceConfiguration(objectMapper, 0, 0);
        objectMapper = ReflectionTestUtils.invokeMethod(serviceConfiguration, "initialiseObjectMapper", objectMapper);
    }

    @Test
    public void ipsStatisticsMapper_someResultJson_deserialise() throws IOException {
        String json = IOUtils.toString(getClass().getResourceAsStream("/api/IpsStatisticsTest/ipsstatistics.json"));
        IpsStatistics expectedIpsStatistics = new IpsStatistics(LocalDate.parse("2019-06-01"), LocalDate.parse("2019-06-30"), 7, 3, 2, 1);


        IpsStatistics actualIpsStatistics = objectMapper.readValue(json, IpsStatistics.class);
        assertThat(actualIpsStatistics).isEqualTo(expectedIpsStatistics);
    }

    @Test
    public void ipsStatisticsMapper_someIpsStats_serialise() throws IOException {
        IpsStatistics ipsStatistics = new IpsStatistics(LocalDate.parse("2019-06-01"), LocalDate.parse("2019-06-30"), 7, 3, 2, 1);
        String expectedJson = IOUtils.toString(getClass().getResourceAsStream("/api/IpsStatisticsTest/ipsstatistics.json"));

        String actualJson = objectMapper.writeValueAsString(ipsStatistics);
        JSONAssert.assertEquals(expectedJson, actualJson, false);
    }
}