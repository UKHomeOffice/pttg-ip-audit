package uk.gov.digital.ho.pttg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.digital.ho.pttg.application.LogEvent.PTTG_AUDIT_FAILURE;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import uk.gov.digital.ho.pttg.api.RequestData;
import uk.gov.digital.ho.pttg.application.ResourceExceptionHandler;

@RunWith(MockitoJUnitRunner.class)
public class ResourceExceptionHandlerTest {

    private ResourceExceptionHandler resourceExceptionHandler;

    @Mock
    private RequestData MockrequestData;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Before
    public void setup() {
        resourceExceptionHandler = new ResourceExceptionHandler(MockrequestData);
        Logger rootLogger = (Logger) LoggerFactory.getLogger(ResourceExceptionHandler.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);
    }

    @Test
    public void shouldHandleAuditException() {

        Exception exception = new Exception("some message");

        ResponseEntity responseEntity = resourceExceptionHandler.handle(exception);

        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(responseEntity.getHeaders()).containsKeys(CONTENT_TYPE);
        assertThat(responseEntity.getHeaders().get(CONTENT_TYPE).size()).isEqualTo(1);
        assertThat(responseEntity.getHeaders().get(CONTENT_TYPE).get(0)).isEqualTo(APPLICATION_JSON_VALUE);
        assertThat(responseEntity.getBody()).isEqualTo("some message");
    }

    @Test
    public void shouldLogErrorForException() {
        Exception mockException = mock(Exception.class);
        when(mockException.getMessage()).thenReturn("any message");

        resourceExceptionHandler.handle(mockException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Audit Exception: any message") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldHandleHttpMessageNotReadableException() {

        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("another message");

        ResponseEntity responseEntity = resourceExceptionHandler.handle(exception);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(responseEntity.getHeaders()).containsKeys(CONTENT_TYPE);
        assertThat(responseEntity.getHeaders().get(CONTENT_TYPE).size()).isEqualTo(1);
        assertThat(responseEntity.getHeaders().get(CONTENT_TYPE).get(0)).isEqualTo(APPLICATION_JSON_VALUE);
        assertThat(responseEntity.getBody()).isEqualTo("another message");
    }

    @Test
    public void shouldLogErrorForHttpMessageNotReadableException() {

        HttpMessageNotReadableException mockException = mock(HttpMessageNotReadableException.class);
        when(mockException.getMessage()).thenReturn("some other message");

        resourceExceptionHandler.handle(mockException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Audit Exception: some other message") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldHandleHttpMessageConversionException() {

        HttpMessageConversionException exception = new HttpMessageConversionException("conversion error message");

        ResponseEntity responseEntity = resourceExceptionHandler.handle(exception);

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(responseEntity.getHeaders()).containsKeys(CONTENT_TYPE);
        assertThat(responseEntity.getHeaders().get(CONTENT_TYPE).size()).isEqualTo(1);
        assertThat(responseEntity.getHeaders().get(CONTENT_TYPE).get(0)).isEqualTo(APPLICATION_JSON_VALUE);
        assertThat(responseEntity.getBody()).isEqualTo("conversion error message");
    }

    @Test
    public void shouldLogErrorForHttpMessageConversionException() {

        HttpMessageNotReadableException mockException = mock(HttpMessageNotReadableException.class);
        when(mockException.getMessage()).thenReturn("conversion error message");

        resourceExceptionHandler.handle(mockException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Audit Exception: conversion error message") &&
                    loggingEvent.getArgumentArray()[1].equals(new ObjectAppendingMarker("event_id", PTTG_AUDIT_FAILURE));
        }));
    }

    @Test
    public void shouldLogRequestDurationOnFaultDetection(){
        Exception mockException = mock(Exception.class);

        resourceExceptionHandler.handle(mockException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("request_duration_ms");
        }));
    }

    @Test
    public void shouldLogRequestDurationOnHttpMessageNotReadableException(){
        Exception mockHttpMessageNotReadableException = mock(HttpMessageNotReadableException.class);

        resourceExceptionHandler.handle(mockHttpMessageNotReadableException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("request_duration_ms");
        }));
    }
}
