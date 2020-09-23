package uk.gov.digital.ho.pttg.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import uk.gov.digital.ho.pttg.AuditService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@WebMvcTest(value = AuditResource.class)
public class RequestDataWebTest {

    @MockBean
    AuditService auditService;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;


    @Test
    public void shouldReturnHttpOkForGet() {
        List<Object> interceptors = (List<Object>)ReflectionTestUtils.getField(handlerMapping, "interceptors");
        assertThat(interceptors.stream().filter(i -> i.getClass().equals(RequestData.class)).count()).isEqualTo(1);

    }

}
