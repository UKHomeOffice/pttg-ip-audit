package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.digital.ho.pttg.api.RequestData;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.digital.ho.pttg.api.RequestData.REQUEST_DURATION_MS;
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.PTTG_AUDIT_FAILURE;

@ControllerAdvice
@Slf4j
public class ResourceExceptionHandler {

    private RequestData requestData;

    public ResourceExceptionHandler(RequestData requestData) {
        this.requestData = requestData;
    }

    @ExceptionHandler
    public ResponseEntity handle(Exception e) {
        log.error("Audit Exception: {}", e.getMessage(),
                value(EVENT, PTTG_AUDIT_FAILURE),
                value(REQUEST_DURATION_MS, requestData.calculateRequestDuration()));
        return errorResponse(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity handle(HttpMessageNotReadableException e) {
        log.error("Audit Exception: {}", e.getMessage(),
                value(EVENT, PTTG_AUDIT_FAILURE),
                value(REQUEST_DURATION_MS, requestData.calculateRequestDuration()));
        return errorResponse(e.getMessage(), BAD_REQUEST);
    }

    private ResponseEntity<String> errorResponse(String message, HttpStatus httpStatus) {
        return new ResponseEntity<>(message, httpHeaders(), httpStatus);
    }

    private HttpHeaders httpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        return headers;
    }
}
