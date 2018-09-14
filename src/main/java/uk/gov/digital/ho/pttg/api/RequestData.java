package uk.gov.digital.ho.pttg.api;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class RequestData implements HandlerInterceptor {

    public static final String SESSION_ID_HEADER = "x-session-id";
    public static final String CORRELATION_ID_HEADER = "x-correlation-id";
    public static final String USER_ID_HEADER = "x-auth-userid";
    static final String USER_HOST = "userHost";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        MDC.clear();

        MDC.put(SESSION_ID_HEADER, initialiseSessionId(request));
        MDC.put(CORRELATION_ID_HEADER, initialiseCorrelationId(request));
        MDC.put(USER_ID_HEADER, initialiseUserName(request));
        MDC.put(USER_HOST, initialiseRemoteHost(request));

        response.setHeader(SESSION_ID_HEADER, sessionId());
        response.setHeader(USER_ID_HEADER, userId());
        response.setHeader(CORRELATION_ID_HEADER, correlationId());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.clear();
    }

    private String initialiseRemoteHost(HttpServletRequest request) {
        String remoteHost = request.getRemoteHost();
        return StringUtils.isNotBlank(remoteHost) ? remoteHost : "unknown";
    }

    private String initialiseSessionId(HttpServletRequest request) {
        String sessionId = WebUtils.getSessionId(request);
        return StringUtils.isNotBlank(sessionId) ? sessionId : "unknown";
    }

    private String initialiseCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        return StringUtils.isNotBlank(correlationId) ? correlationId : UUID.randomUUID().toString();
    }

    private String initialiseUserName(HttpServletRequest request) {
        String userId = request.getHeader(USER_ID_HEADER);
        return StringUtils.isNotBlank(userId) ? userId : "anonymous";
    }

    public String sessionId() {
        return MDC.get(SESSION_ID_HEADER);
    }

    public String correlationId() {
        return MDC.get(CORRELATION_ID_HEADER);
    }

    public String userId() {
        return MDC.get(USER_ID_HEADER);
    }
}
