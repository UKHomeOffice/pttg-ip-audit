package uk.gov.digital.ho.pttg.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.digital.ho.pttg.AuditEventType;
import uk.gov.digital.ho.pttg.AuditHistoryService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_REQUEST;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_RESPONSE;

@RunWith(SpringRunner.class)
@WebMvcTest(value = AuditHistoryResource.class)
public class AuditHistoryWebTest {

    private final static String HISTORY_URL = "/history";
    private final static String CORRELATION_ID_URL = "/correlationIds";
    private static final String HISTORY_BY_CORRELATION_ID_URL = "/historyByCorrelationId";

    private static final LocalDate REQ_DATE = LocalDate.now();
    private static final String REQ_DATE_PARAM = REQ_DATE.format(DateTimeFormatter.ISO_DATE);
    private static final List<AuditEventType> EVENT_TYPES = Arrays.asList(
            INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
    private static final LocalDate TO_DATE = LocalDate.now();

    private static final String EVENT_TYPES_PARAM = String.format("%s,%s",
                                                                  INCOME_PROVING_FINANCIAL_STATUS_REQUEST, INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
    private static final String REQ_CORRELATION_ID_PARAM = "some correlation id";
    private static final String TO_DATE_PARAM = TO_DATE.toString();

    @MockBean
    private AuditHistoryService mockAuditHistoryService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void retrieveAuditHistory_returnsOk() throws Exception {
        mockMvc.perform(get(HISTORY_URL)
                                .param("toDate", REQ_DATE_PARAM)
                                .param("eventTypes", EVENT_TYPES_PARAM)
                       )
               .andExpect(status().isOk());
    }

    @Test
    public void retrieveAuditHistory_callsService() throws Exception {
        mockMvc.perform(get(HISTORY_URL)
                                .param("toDate", REQ_DATE_PARAM)
                                .param("eventTypes", EVENT_TYPES_PARAM)
                       );

        Pageable defaultPageable = PageRequest.of(0, 20);
        verify(mockAuditHistoryService).getAuditHistory(REQ_DATE, EVENT_TYPES, defaultPageable);
    }

    @Test
    public void retrieveAuditHistory_badDateParam() throws Exception {
        mockMvc.perform(get(HISTORY_URL)
                                .param("toDate", REQ_DATE_PARAM + "bad")
                                .param("eventTypes", EVENT_TYPES_PARAM)
                       )
               .andExpect(status().isBadRequest());
    }

    @Test
    public void retrieveAuditHistory_badEventTypesParam() throws Exception {
        mockMvc.perform(get(HISTORY_URL)
                                .param("toDate", REQ_DATE_PARAM)
                                .param("eventTypes", "This is not an event type")
                       )
               .andExpect(status().isBadRequest());
    }

    @Test
    public void retrieveAuditHistory_missingEventType() throws Exception {
        mockMvc.perform(get(HISTORY_URL)
                                .param("toDate", REQ_DATE_PARAM)
                       )
               .andExpect(status().isBadRequest());
    }

    @Test
    public void retrieveAuditHistory_missingParams() throws Exception {
        mockMvc.perform(get(HISTORY_URL))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void getCorrelationIds_returnsOk() throws Exception {
        mockMvc.perform(get(CORRELATION_ID_URL)
                                .param("eventTypes", EVENT_TYPES_PARAM))
               .andExpect(status().isOk());
    }

    @Test
    public void getCorrelationIds_callsService() throws Exception {
        mockMvc.perform(get(CORRELATION_ID_URL)
                                .param("eventTypes", EVENT_TYPES_PARAM));

        verify(mockAuditHistoryService).getAllCorrelationIds(EVENT_TYPES);
    }

    @Test
    public void getCorrelationIds_badEventTypesParam() throws Exception {
        mockMvc.perform(get(CORRELATION_ID_URL)
                                .param("eventTypes", "this is not an event type"))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void getCorrelationIds_missingEventType() throws Exception {
        mockMvc.perform(get(CORRELATION_ID_URL))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void getCorrelationIds_contentTypeJson() throws Exception {
        mockMvc.perform(get(CORRELATION_ID_URL)
                                .param("eventTypes", EVENT_TYPES_PARAM))
               .andExpect(header().string("Content-Type", "application/json"));
    }

    @Test
    public void getCorrelationIds_withToDate_returnsOK() throws Exception {
        mockMvc.perform(get(CORRELATION_ID_URL)
                                .param("eventTypes", EVENT_TYPES_PARAM)
                                .param("toDate", TO_DATE_PARAM))
               .andExpect(status().isOk());
    }

    @Test
    public void getCorrelationIds_withToDate_callsService() throws Exception {
        mockMvc.perform(get(CORRELATION_ID_URL)
                                .param("eventTypes", EVENT_TYPES_PARAM)
                                .param("toDate", TO_DATE_PARAM));

        then(mockAuditHistoryService).should().getAllCorrelationIds(EVENT_TYPES, TO_DATE);
    }

    @Test
    public void getCorrelationIds_withToDate_badEventTypesParam() throws Exception {
        mockMvc.perform(get(CORRELATION_ID_URL)
                                .param("eventTypes", "this is not an event type")
                                .param("toDate", TO_DATE_PARAM))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void getCorrelationIds_withToDate_badToDateParam() throws Exception {
        mockMvc.perform(get(CORRELATION_ID_URL)
                                .param("eventTypes", EVENT_TYPES_PARAM)
                                .param("toDate", "invalid date"))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void getCorrelationIds_withToDate_missingEventType() throws Exception {
        mockMvc.perform(get(CORRELATION_ID_URL)
                                .param("toDate", TO_DATE_PARAM))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void getCorrelationIds_withToDate_contentTypeJson() throws Exception {
        mockMvc.perform(get(CORRELATION_ID_URL)
                                .param("eventTypes", EVENT_TYPES_PARAM)
                                .param("toDate", TO_DATE_PARAM))
               .andExpect(header().string("Content-Type", "application/json"));
    }

    @Test
    public void getRecordsForCorrelationId_returnsOk() throws Exception {
        mockMvc.perform(get(HISTORY_BY_CORRELATION_ID_URL)
                                .param("correlationId", REQ_CORRELATION_ID_PARAM)
                                .param("eventTypes", EVENT_TYPES_PARAM)
                       )
               .andExpect(status().isOk());
    }

    @Test
    public void getRecordsForCorrelationId_callsService() throws Exception {
        mockMvc.perform(get(HISTORY_BY_CORRELATION_ID_URL)
                                .param("correlationId", REQ_CORRELATION_ID_PARAM)
                                .param("eventTypes", EVENT_TYPES_PARAM));

        verify(mockAuditHistoryService).getRecordsForCorrelationId(REQ_CORRELATION_ID_PARAM, EVENT_TYPES);
    }

    @Test
    public void getRecordsForCorrelationId_badEventTypesParam() throws Exception {
        mockMvc.perform(get(HISTORY_BY_CORRELATION_ID_URL)
                                .param("correlationId", REQ_CORRELATION_ID_PARAM)
                                .param("eventTypes", "This is not an event type")
                       )
               .andExpect(status().isBadRequest());
    }

    @Test
    public void getRecordsForCorrelationId_missingCorrelationId() throws Exception {
        mockMvc.perform(get(HISTORY_BY_CORRELATION_ID_URL)
                                .param("eventTypes", EVENT_TYPES_PARAM)
                       )
               .andExpect(status().isBadRequest());
    }

    @Test
    public void getRecordsForCorrelationId_missingEventType() throws Exception {
        mockMvc.perform(get(HISTORY_BY_CORRELATION_ID_URL)
                                .param("correlationId", REQ_CORRELATION_ID_PARAM)
                       )
               .andExpect(status().isBadRequest());
    }

    @Test
    public void getRecordsForCorrelationId_contentTypeJson() throws Exception {
        mockMvc.perform(get(HISTORY_BY_CORRELATION_ID_URL)
                                .param("correlationId", REQ_CORRELATION_ID_PARAM)
                                .param("eventTypes", EVENT_TYPES_PARAM)
                       )
               .andExpect(header().string("Content-Type", "application/json"));
    }
}
