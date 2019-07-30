package uk.gov.digital.ho.pttg.api;

import org.apache.commons.io.IOUtils;
import org.assertj.core.util.Files;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.digital.ho.pttg.IpsStatisticsService;

import java.io.FileInputStream;
import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(Parameterized.class)
@WebMvcTest(value = IpsStatisticsResource.class)
public class IpsStatisticsResource_StoreIpsStats_BadRequests_WebTest {

    @ClassRule
    public static final SpringClassRule springClassRule = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @MockBean private IpsStatisticsService mockService;

    @Autowired private MockMvc mockMvc;

    @Parameter
    public String fileName;

    @Parameters(name = "Should get a bad request trying to store IPS statistics: {0}")
    public static Iterable<String> testData() {
        return Files.fileNamesIn("src/test/resources/api/IpsStatisticsTest/badrequests", false);
    }

    @Test
    public void storeIpsStatistics_invalidRequest_badRequest400() throws Exception {
        mockMvc.perform(post("/ipsstatistics")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loadJsonRequest(fileName)))
               .andExpect(status().isBadRequest());
    }

    private String loadJsonRequest(String fileName) throws IOException {
        return IOUtils.toString(new FileInputStream(fileName));
    }
}