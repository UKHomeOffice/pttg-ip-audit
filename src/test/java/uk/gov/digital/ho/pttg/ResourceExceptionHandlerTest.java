package uk.gov.digital.ho.pttg;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class ResourceExceptionHandlerTest {

    private ResourceExceptionHandler resourceExceptionHandler;

    @Before
    public void setup() {
        resourceExceptionHandler = new ResourceExceptionHandler();
    }

    @Test
    public void shouldHandleAuditException() {

        AuditException exception = new AuditException("some message");

        ResponseEntity responseEntity = resourceExceptionHandler.handle(exception);

        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(responseEntity.getHeaders()).containsKeys(CONTENT_TYPE);
        assertThat(responseEntity.getHeaders().get(CONTENT_TYPE).size()).isEqualTo(1);
        assertThat(responseEntity.getHeaders().get(CONTENT_TYPE).get(0)).isEqualTo(APPLICATION_JSON_VALUE);
        assertThat(responseEntity.getBody()).isEqualTo("some message");
    }
}