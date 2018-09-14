package uk.gov.digital.ho.pttg.api;

import org.jboss.logging.MDC;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.pttg.api.RequestData.*;

public class RequestDataTest {

    private RequestData requestData = new RequestData();


    @Test
    public void shouldUseCollaborators() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        Object mockHandler = mock(Object.class);

        when(mockRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn("some correlation id");
        when(mockRequest.getHeader(USER_ID_HEADER)).thenReturn("some user id");

        requestData.preHandle(mockRequest, mockResponse, mockHandler);

        verify(mockRequest).getHeader(CORRELATION_ID_HEADER);
        verify(mockRequest).getHeader(USER_ID_HEADER);
        verify(mockRequest).getRemoteHost();

        verify(mockResponse).setHeader(SESSION_ID_HEADER, "unknown");
        verify(mockResponse).setHeader(USER_ID_HEADER, "some user id");
        verify(mockResponse).setHeader(CORRELATION_ID_HEADER, "some correlation id");
    }

    @Test
    public void shouldSetupDefaultMdc() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        Object mockHandler = mock(Object.class);

        requestData.preHandle(mockRequest, mockResponse, mockHandler);

        assertThat(MDC.get(SESSION_ID_HEADER)).isEqualTo("unknown");
        assertThat(MDC.get(CORRELATION_ID_HEADER)).isNotNull();
        assertThat(MDC.get(USER_ID_HEADER)).isEqualTo("anonymous");
        assertThat(MDC.get(USER_HOST)).isEqualTo("unknown");
    }

    @Test
    public void shouldExposeDefaultMdc() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        Object mockHandler = mock(Object.class);

        requestData.preHandle(mockRequest, mockResponse, mockHandler);

        assertThat(requestData.sessionId()).isEqualTo("unknown");
        assertThat(requestData.correlationId()).isNotNull();
        assertThat(requestData.userId()).isEqualTo("anonymous");
    }

    @Test
    public void shouldExposeMdc() {

        HttpSession mockHttpSession = mock(HttpSession.class);
        when(mockHttpSession.getId()).thenReturn("a session id");

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getSession(false)).thenReturn(mockHttpSession);
        when(mockRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn("a correlation id");
        when(mockRequest.getHeader(USER_ID_HEADER)).thenReturn("a user id");

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        Object mockHandler = mock(Object.class);

        requestData.preHandle(mockRequest, mockResponse, mockHandler);

        assertThat(requestData.sessionId()).isEqualTo("a session id");
        assertThat(requestData.correlationId()).isEqualTo("a correlation id");
        assertThat(requestData.userId()).isEqualTo("a user id");
    }

}
