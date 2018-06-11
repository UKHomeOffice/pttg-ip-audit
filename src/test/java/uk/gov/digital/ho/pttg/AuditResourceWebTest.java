package uk.gov.digital.ho.pttg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.digital.ho.pttg.api.AuditResource;
import uk.gov.digital.ho.pttg.api.AuditableData;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.data.domain.Sort.unsorted;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.digital.ho.pttg.AuditEventType.INCOME_PROVING_FINANCIAL_STATUS_REQUEST;

@RunWith(SpringRunner.class)
@WebMvcTest(value = AuditResource.class, secure = false)
public class AuditResourceWebTest {

    private static final String AUDIT_URL = "/audit";

    @MockBean AuditService auditService;

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Test
    public void shouldReturnHttpOkForGet() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(AUDIT_URL))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldCallServiceToRetrieveDataWithDefaultPaging() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(AUDIT_URL));

        verify(auditService).getAllAuditData(eq(PageRequest.of(0, 20, unsorted())));
    }

    @Test
    public void shouldCallServiceToRetrieveDataWithSpecificPaging() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(AUDIT_URL + "?page=10&size=10"))
                .andExpect(status().isOk());

        verify(auditService).getAllAuditData(eq(PageRequest.of(10, 10, unsorted())));
    }

    @Test
    public void shouldReturnHttpBadRequestForPostWhenMissingMandatoryData() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(AUDIT_URL)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnHttpOkForPost() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(AUDIT_URL)
                .content(createAuditableData())
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldCallServiceToRecordAuditEntry() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(AUDIT_URL)
                .content(createAuditableData())
                .contentType(APPLICATION_JSON));

        verify(auditService).add(any(AuditableData.class));
    }

    private String createAuditableData() throws JsonProcessingException {

        AuditableData auditableData = new AuditableData("some uuid",
                LocalDateTime.of(2017, 9, 11, 14, 45, 48),
                "some session id",
                "3a22c723-ea0f-4962-b97b-f35dce3284b2",
                "bobby.bag@digital.homeoffice.gov.uk",
                "some deployment",
                "some namespace",
                INCOME_PROVING_FINANCIAL_STATUS_REQUEST,
                "{}");

        return objectMapper.writeValueAsString(auditableData);
    }
}
