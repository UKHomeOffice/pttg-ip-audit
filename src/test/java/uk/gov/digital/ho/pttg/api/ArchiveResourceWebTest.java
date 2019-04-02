package uk.gov.digital.ho.pttg.api;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.digital.ho.pttg.ArchiveService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = ArchiveResource.class)
public class ArchiveResourceWebTest {

    @MockBean private ArchiveService archiveService;

    @Autowired private MockMvc mockMvc;

    private final static String ANY_NINO = "any_nino";
    private final static String ANY_RESULT = "any_result";
    private final static String ANY_EVENT_ID = "any-event-id";
    private final static LocalDate ANY_RESULT_DATE = LocalDate.now().minusDays(2);
    private final static LocalDate ANY_LAST_ARCHIVE_DATE = LocalDate.now().minusDays(1);

    @Test
    public void archiveNino_basicRequest_returnsOk() throws Exception {
        mockMvc.perform(post(String.format("/nino/%s/archive/%s", ANY_NINO, ANY_RESULT))
                .content(loadJsonRequest(ANY_LAST_ARCHIVE_DATE, Collections.singletonList(ANY_EVENT_ID), ANY_RESULT_DATE))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private String loadJsonRequest(LocalDate lastArchiveDate, List<String> eventIds,LocalDate resultDate) throws IOException {
        String template = IOUtils.toString(getClass().getResourceAsStream("/api/ArchiveResourceWebTest/basic-request.json"));
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        StringBuilder events = new StringBuilder();
        int count = 0;
        for (String eventId : eventIds) {
            events.append("\"");
            events.append(eventId);
            events.append("\"");
            if (count < eventIds.size()-1) {
                events.append(",");
            }
        }
        return String.format(template, formatter.format(lastArchiveDate), events.toString(), formatter.format(resultDate));
    }

}
