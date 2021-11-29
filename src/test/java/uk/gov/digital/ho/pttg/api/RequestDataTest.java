package uk.gov.digital.ho.pttg.api;

import org.jboss.logging.MDC;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.pttg.api.RequestData.*;

@RunWith(MockitoJUnitRunner.class)
public class RequestDataTest {

    private final RequestData requestData = new RequestData();

    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private Object mockHandler;
    @Mock
    private HttpHeaders mockHeaders;

    @Test
    public void shouldUseCollaborators() {
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
        requestData.preHandle(mockRequest, mockResponse, mockHandler);

        assertThat(MDC.get(SESSION_ID_HEADER)).isEqualTo("unknown");
        assertThat(MDC.get(CORRELATION_ID_HEADER)).isNotNull();
        assertThat(MDC.get(USER_ID_HEADER)).isEqualTo("anonymous");
        assertThat(MDC.get(USER_HOST)).isEqualTo("unknown");
    }

    @Test
    public void shouldExposeDefaultMdc() {
        requestData.preHandle(mockRequest, mockResponse, mockHandler);

        assertThat(requestData.sessionId()).isEqualTo("unknown");
        assertThat(requestData.correlationId()).isNotNull();
        assertThat(requestData.userId()).isEqualTo("anonymous");
    }

    @Test
    public void shouldExposeMdc() {

        HttpSession mockHttpSession = mock(HttpSession.class);
        when(mockHttpSession.getId()).thenReturn("a session id");

        when(mockRequest.getSession(false)).thenReturn(mockHttpSession);
        when(mockRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn("a correlation id");
        when(mockRequest.getHeader(USER_ID_HEADER)).thenReturn("a user id");

        requestData.preHandle(mockRequest, mockResponse, mockHandler);

        assertThat(requestData.sessionId()).isEqualTo("a session id");
        assertThat(requestData.correlationId()).isEqualTo("a correlation id");
        assertThat(requestData.userId()).isEqualTo("a user id");
    }

    @Test
    public void shouldAddRequestTimestampToMDC() {
        requestData.preHandle(mockRequest, mockResponse, mockHandler);

        assertThat(MDC.get("request-timestamp")).isNotNull();
    }

    @Test
    public void shouldReturnRequestDuration() {
        requestData.preHandle(mockRequest, mockResponse, mockHandler);

        assertThat(requestData.calculateRequestDuration()).isNotNegative();
    }

    @Test
    public void preHandle_noComponentTraceHeader_create() {
        when(mockRequest.getHeader("x-component-trace")).thenReturn(null);

        requestData.preHandle(mockRequest, mockResponse, mockHandler);

        assertThat(requestData.componentTrace()).isEqualTo("pttg-ip-audit");
    }

    @Test
    public void preHandle_componentTraceHeader_append() {
        when(mockRequest.getHeader("x-component-trace")).thenReturn("pttg-ip-api");

        requestData.preHandle(mockRequest, mockResponse, mockHandler);

        assertThat(requestData.componentTrace()).isEqualTo("pttg-ip-api,pttg-ip-audit");
    }

    @Test
    public void preHandle_componentTraceHeaderMultipleComponents_append() {
        when(mockRequest.getHeader("x-component-trace")).thenReturn("pttg-ip-api,pttg-ip-hmrc");

        requestData.preHandle(mockRequest, mockResponse, mockHandler);

        assertThat(requestData.componentTrace()).isEqualTo("pttg-ip-api,pttg-ip-hmrc,pttg-ip-audit");
    }

    @Test
    public void addComponentTraceHeader_anyResponse_addsHeader() {
        String expectedComponentTrace = "some-component,some-other-component";
        org.slf4j.MDC.put(COMPONENT_TRACE_HEADER, expectedComponentTrace);

        requestData.addComponentTraceHeader(mockHeaders);

        then(mockHeaders).should().add(COMPONENT_TRACE_HEADER, expectedComponentTrace);
    }

    @Test
    public void postHandle_someComponentTrace_addAsHeader() throws Exception {
        String expectedComponentTrace = "pttg-ip-api,pttg-ip-audit";
        MDC.put(COMPONENT_TRACE_HEADER, expectedComponentTrace);

        ModelAndView mockModelAndView = mock(ModelAndView.class);
        requestData.postHandle(mockRequest, mockResponse, mockHandler, mockModelAndView);

        then(mockResponse).should().addHeader(COMPONENT_TRACE_HEADER, expectedComponentTrace);
    }
}
