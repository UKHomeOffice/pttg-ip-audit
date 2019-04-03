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
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = ArchiveResource.class)
public class ArchiveResourceWebTest {

    @MockBean private ArchiveService archiveService;

    @Autowired private MockMvc mockMvc;

    private final static String ANY_RESULT = "any_result";
    private final static String ANY_EVENT_ID = "any-event-id";
    private final static LocalDate ANY_RESULT_DATE = LocalDate.now().minusDays(2);
    private final static LocalDate ANY_LAST_ARCHIVE_DATE = LocalDate.now().minusDays(1);

    @Test
    public void archiveResult_basicRequest_returnsOk() throws Exception {
        mockMvc.perform(post(String.format("/archive/%s", ANY_RESULT_DATE))
                .content(loadJsonRequest(ANY_RESULT, ANY_LAST_ARCHIVE_DATE, singletonList(ANY_EVENT_ID)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void archiveResult_basicRequest_callsService() throws Exception {
        List<String> eventIds = singletonList(ANY_EVENT_ID);
        mockMvc.perform(post(String.format("/archive/%s", ANY_RESULT_DATE))
                .content(loadJsonRequest(ANY_RESULT, ANY_LAST_ARCHIVE_DATE, eventIds))
                .contentType(MediaType.APPLICATION_JSON));

        verify(archiveService).archiveResult(ANY_RESULT_DATE, ANY_RESULT, eventIds, ANY_LAST_ARCHIVE_DATE);
    }

    @Test
    public void archiveResult_multipleEventIds_returnsOk() throws Exception {
        List<String> eventIds = asList(ANY_EVENT_ID, ANY_EVENT_ID);
        mockMvc.perform(post(String.format("/archive/%s", ANY_RESULT_DATE))
                .content(loadJsonRequest(ANY_RESULT, ANY_LAST_ARCHIVE_DATE, eventIds))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void archiveResult_malformedUrl_clientError() throws Exception {
        mockMvc.perform(post(String.format("/bad/%s", ANY_RESULT_DATE))
                .content(loadJsonRequest(ANY_RESULT, ANY_LAST_ARCHIVE_DATE, singletonList(ANY_EVENT_ID)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void archiveResult_missingResultDate_clientError() throws Exception {
        mockMvc.perform(post(String.format("/archive/%s", ""))
                .content(loadJsonRequest(ANY_RESULT, ANY_LAST_ARCHIVE_DATE, singletonList(ANY_EVENT_ID)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void archiveResult_malformedResultDate_clientError() throws Exception {
        mockMvc.perform(post(String.format("/archive/%s", "im_not_a_date"))
                .content(loadJsonRequest(ANY_RESULT, ANY_LAST_ARCHIVE_DATE, singletonList(ANY_EVENT_ID)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void archiveResult_missingBody_clientError() throws Exception {
        mockMvc.perform(post(String.format("/archive/%s", ANY_RESULT_DATE))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void archiveResult_malformedBody_clientError() throws Exception {
        mockMvc.perform(post(String.format("/archive/%s", ANY_RESULT_DATE))
                .content("im_not_valid_json")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void archiveResult_missingResult_clientError() throws Exception {
        mockMvc.perform(post(String.format("/archive/%s", ANY_RESULT_DATE))
                .content(loadJsonRequest("missing-result.json"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void archiveResult_emptyResult_clientError() throws Exception {
        mockMvc.perform(post(String.format("/archive/%s", ANY_RESULT_DATE))
                .content(loadJsonRequest("", ANY_LAST_ARCHIVE_DATE, singletonList(ANY_EVENT_ID)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void archiveResult_nullResultValue_clientError() throws Exception {
        mockMvc.perform(post(String.format("/archive/%s", ANY_RESULT_DATE))
                .content(loadJsonRequest("null-result-value.json"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void archiveResult_missingArchiveDate_clientError() throws Exception {
        mockMvc.perform(post(String.format("/archive/%s", ANY_RESULT_DATE))
                .content(loadJsonRequest("missing-archive-date.json"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void archiveResult_emptyArchiveDate_clientError() throws Exception {
        mockMvc.perform(post(String.format("/archive/%s", ANY_RESULT_DATE))
                .content(loadJsonRequest(ANY_RESULT, "", singletonList(ANY_EVENT_ID).toString()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void archiveResult_nullArchiveDate_clientError() throws Exception {
        mockMvc.perform(post(String.format("/archive/%s", ANY_RESULT_DATE))
                .content(loadJsonRequest("null-archive-date-value.json"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void archiveResult_invalidArchiveDate_clientError() throws Exception {
        mockMvc.perform(post(String.format("/archive/%s", ANY_RESULT_DATE))
                .content(loadJsonRequest(ANY_RESULT, "2019-55-66", singletonList(ANY_EVENT_ID).toString()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void archiveResult_missingEventIds_clientError() throws Exception {
        mockMvc.perform(post(String.format("/archive/%s", ANY_RESULT_DATE))
                .content(loadJsonRequest("missing-eventids.json"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void archiveResult_emptyEventIds_clientError() throws Exception {
        mockMvc.perform(post(String.format("/archive/%s", ANY_RESULT_DATE))
                .content(loadJsonRequest("empty-eventids.json"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void archiveResult_nullEventIds_clientError() throws Exception {
        mockMvc.perform(post(String.format("/archive/%s", ANY_RESULT_DATE))
                .content(loadJsonRequest("null-eventids-value.json"))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    private String loadJsonRequest(String result, LocalDate lastArchiveDate, List<String> eventIds) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        StringBuilder events = new StringBuilder();
        formatEventsIntoJsonArray(eventIds, events);
        return loadJsonRequest(result, formatter.format(lastArchiveDate), events.toString());
    }

    private String loadJsonRequest(String result, String lastArchiveDate, String events) throws IOException {
        String template = IOUtils.toString(getClass().getResourceAsStream("/api/ArchiveResourceWebTest/basic-request.json"));
        return String.format(template, result, lastArchiveDate, events);
    }

    private String loadJsonRequest(String file) throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream("/api/ArchiveResourceWebTest/" + file));
    }

    private void formatEventsIntoJsonArray(List<String> eventIds, StringBuilder events) {
        int count = 0;
        for (String eventId : eventIds) {
            events.append("\"");
            events.append(eventId);
            events.append("\"");
            if (count++ < eventIds.size()-1) {
                events.append(",");
            }
        }
    }

}
