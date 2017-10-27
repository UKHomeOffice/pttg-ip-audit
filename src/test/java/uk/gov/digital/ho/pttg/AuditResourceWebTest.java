package uk.gov.digital.ho.pttg;

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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = AuditResource.class, secure = false)
public class AuditResourceWebTest {

    private static final String AUDIT_URL = "/audit";

    @MockBean AuditService auditService;

    @Autowired private MockMvc mockMvc;

    @Test
    public void shouldReturnHttpOkForGet() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(AUDIT_URL))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldCallServiceToRetrieveDataWithDefaultPaging() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(AUDIT_URL));

        verify(auditService).getAllAuditData(eq(new PageRequest(0, 20, null)));
    }

    @Test
    public void shouldCallServiceToRetrieveDataWithSpecificPaging() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(AUDIT_URL + "?page=10&size=10"))
                .andExpect(status().isOk());

        verify(auditService).getAllAuditData(eq(new PageRequest(10, 10, null)));
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

    private String createAuditableData() {
        return "{      " +
                "      \"eventId\" : \"some uuid\"," +
                "      \"timestamp\" : \"2017-09-11T14:45:48.094\"," +
                "      \"sessionId\" : \"some session id\"," +
                "      \"correlationId\" : \"3a22c723-ea0f-4962-b97b-f35dce3284b2\"," +
                "      \"userId\" : \"bobby.bag@digital.homeoffice.gov.uk\"," +
                "      \"deploymentName\" : \"some deployment\"," +
                "      \"deploymentNamespace\" : \"some namespace\"," +
                "      \"eventType\" : \"INCOME_PROVING_FINANCIAL_STATUS_REQUEST\"," +
                "       \"data\" : \"{}\" " +
                "}";
    }
}
