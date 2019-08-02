package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext
public class IpsStatisticsResourceIntTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void ipsStatisticsResource_savedStatistics_canRetrieve() {
        LocalDate someFromDate = LocalDate.parse("2019-03-01");
        LocalDate someToDate = LocalDate.parse("2019-03-31");
        IpsStatistics storedStatistics = new IpsStatistics(someFromDate, someToDate, 3, 5, 1, 2);
        restTemplate.postForEntity("/ipsstatistics", storedStatistics, Void.class);

        IpsStatistics retrievedStatistics = restTemplate.getForObject("/ipsstatistics?fromDate={fromDate}&toDate={toDate}", IpsStatistics.class, someFromDate, someToDate);
        assertThat(retrievedStatistics).isEqualTo(storedStatistics);
    }
}