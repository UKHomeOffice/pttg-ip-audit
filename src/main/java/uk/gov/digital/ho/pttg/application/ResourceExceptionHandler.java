package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@ControllerAdvice
@Slf4j
public class ResourceExceptionHandler {

    @ExceptionHandler(AuditException.class)
    public ResponseEntity handle(AuditException e) {
        log.error("AuditException: {}", e.getMessage(), value(EVENT, PTTG_AUDIT_FAILURE));
        return errorResponse(e.getMessage(), INTERNAL_SERVER_ERROR);
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