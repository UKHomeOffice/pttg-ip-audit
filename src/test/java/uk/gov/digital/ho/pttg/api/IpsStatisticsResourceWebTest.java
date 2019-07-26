package uk.gov.digital.ho.pttg.api;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.digital.ho.pttg.IpsStatisticsService;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.digital.ho.pttg.IpsStatisticsService.NO_STATISTICS;

@RunWith(SpringRunner.class)
@WebMvcTest(value = IpsStatisticsResource.class)
public class IpsStatisticsResourceWebTest {

    @MockBean private IpsStatisticsService mockService;

    @Autowired private MockMvc mockMvc;

    private static final String GET_STATS_URL = "/ipsstatistics?fromDate={fromDate}&toDate={toDate}";
    private static final String STORE_STATS_URL = "/ipsstatistics";
    private static final LocalDate ANY_DATE = LocalDate.now();

    @Test
    public void getIpsStatistics_statsFound_returnsOk() throws Exception {
        IpsStatistics anyStatistics = new IpsStatistics(ANY_DATE, ANY_DATE, 3, 1, 2, 1);

        given(mockService.getIpsStatistics(any(), any())).willReturn(anyStatistics);
        mockMvc.perform(get(GET_STATS_URL, ANY_DATE, ANY_DATE))
               .andExpect(status().isOk());
    }

    @Test
    public void getIpsStatistics_noStatsFound_returnsNotFound() throws Exception {
        given(mockService.getIpsStatistics(any(), any())).willReturn(NO_STATISTICS);
        mockMvc.perform(get(GET_STATS_URL, ANY_DATE, ANY_DATE))
               .andExpect(status().isNotFound());
    }

    @Test
    public void getIpsStatistics_noFromDate_badRequest() throws Exception {
        mockMvc.perform(get("/ipsstatistics?toDate={toDate}", ANY_DATE))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void getIpsStatistics_noToDate_badRequest() throws Exception {
        mockMvc.perform(get("/ipsstatistics?fromDate={fromDate}", ANY_DATE))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void getIpsStatistics_nullFromDate_badRequest() throws Exception {
        mockMvc.perform(get(GET_STATS_URL, null, ANY_DATE))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void getIpsStatistics_nullToDate_badRequest() throws Exception {
        mockMvc.perform(get(GET_STATS_URL, ANY_DATE, null))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void getIpsStatistics_malformedFromDate_badRequest() throws Exception {
        mockMvc.perform(get(GET_STATS_URL, "not a valid date", ANY_DATE))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void getIpsStatistics_malformedToDate_badRequest() throws Exception {
        mockMvc.perform(get(GET_STATS_URL, ANY_DATE, "not a valid date"))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void saveIpsStatistics_basicRequest_returnOk() throws Exception {
        mockMvc.perform(post(STORE_STATS_URL)
                                .content(loadJsonRequest())
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());
    }

    @Test
    public void saveIpsStatistics_basicRequest_storeStatistics() throws Exception {
        mockMvc.perform(post(STORE_STATS_URL)
                                .content(loadJsonRequest())
                                .contentType(MediaType.APPLICATION_JSON));

        ArgumentCaptor<IpsStatistics> argumentCaptor = ArgumentCaptor.forClass(IpsStatistics.class);
        then(mockService).should().storeIpsStatistics(argumentCaptor.capture());

        IpsStatistics expectedStatistics = new IpsStatistics(LocalDate.parse("2019-06-01"), LocalDate.parse("2019-06-30"),
                                                             7, 3, 2, 1);
        assertThat(argumentCaptor.getValue()).isEqualTo(expectedStatistics);
    }

    @Test
    public void saveIpsStatistics_noBody_badRequest() throws Exception {
        mockMvc.perform(post(STORE_STATS_URL))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void saveIpsStatistics_malformed_badRequest() throws Exception {
        mockMvc.perform(post(STORE_STATS_URL)
                                .content("not a valid request")
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest());
    }

    private String loadJsonRequest() throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream("/api/IpsStatisticsTest/ipsstatistics.json"));
    }
}
