package uk.gov.digital.ho.pttg.application;

public enum LogEvent {
    PTTG_AUDIT_SERVICE_STARTED,
    PTTG_AUDIT_REQUEST_RECEIVED,
    PTTG_AUDIT_RESPONSE_SUCCESS,
    PTTG_AUDIT_RETRIEVAL_REQUEST_RECEIVED,
    PTTG_AUDIT_RETRIEVAL_RESPONSE_SUCCESS,
    PTTG_AUDIT_FAILURE;


    public static final String EVENT = "event_id";
}