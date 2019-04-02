package uk.gov.digital.ho.pttg.api;

import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.digital.ho.pttg.AuditEventType;
import uk.gov.digital.ho.pttg.AuditHistoryService;
import uk.gov.digital.ho.pttg.api.AuditHistoryResource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_REQUEST;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE;

@RunWith(SpringRunner.class)
@WebMvcTest(value = AuditHistoryResource.class)
public class AuditHistoryWebTest {

    private final static String BASE_URL = "/history";

    private static final LocalDate REQ_DATE = LocalDate.now();
    private static final String REQ_DATE_PARAM = REQ_DATE.format(DateTimeFormatter.ISO_DATE);
    private static final List<AuditEventType> EVENT_TYPES = Arrays.asList(
            INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
    private static final String EVENT_TYPES_PARAM = String.format("%s,%s",
            INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);

    @MockBean
    private AuditHistoryService mockAuditHistoryService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void retrieveAuditHistory_returnsOk() throws Exception {
        mockMvc.perform(get(BASE_URL)
                    .param("toDate", REQ_DATE_PARAM)
                    .param("eventTypes", EVENT_TYPES_PARAM)
                )
                .andExpect(status().isOk());
    }

    @Test
    public void retrieveAuditHistory_callsService() throws Exception {
        mockMvc.perform(get(BASE_URL)
                    .param("toDate", REQ_DATE_PARAM)
                    .param("eventTypes", EVENT_TYPES_PARAM)
                );

        verify(mockAuditHistoryService).getAuditHistory(REQ_DATE, EVENT_TYPES);
    }

    @Test
    public void retrieveAuditHistory_badDateParam() throws Exception {
        mockMvc.perform(get(BASE_URL)
                    .param("toDate", REQ_DATE_PARAM + "bad")
                    .param("eventTypes", EVENT_TYPES_PARAM)
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void retrieveAuditHistory_badEventTypesParam() throws Exception {
        mockMvc.perform(get(BASE_URL)
                    .param("toDate", REQ_DATE_PARAM)
                    .param("eventTypes", "This is not an event type")
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void retrieveAuditHistory_missingDate() throws Exception {
        mockMvc.perform(get(BASE_URL)
                    .param("eventTypes", "This is not an event type")
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void retrieveAuditHistory_missingEventType() throws Exception {
        mockMvc.perform(get(BASE_URL)
                    .param("toDate", REQ_DATE_PARAM)
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void retrieveAuditHistory_missingParams() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().is4xxClientError());
    }

}
