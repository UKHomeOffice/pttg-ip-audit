package uk.gov.digital.ho.pttg.application;

public enum LogEvent {
    PTTG_AUDIT_SERVICE_STARTED,
    PTTG_AUDIT_REQUEST_RECEIVED,
    PTTG_AUDIT_REQUEST_COMPLETED,
    PTTG_AUDIT_RESPONSE_SUCCESS,
    PTTG_AUDIT_RETRIEVAL_REQUEST_RECEIVED,
    PTTG_AUDIT_RETRIEVAL_RESPONSE_SUCCESS,
    PTTG_AUDIT_FAILURE,
    PTTG_AUDIT_CONFIG_MISMATCH,
    PTTG_AUDIT_HISTORY_REQUEST_RECEIVED,
    PTTG_AUDIT_HISTORY_RESPONSE_SUCCESS,
    PTTG_AUDIT_HISTORY_CORRELATION_IDS_REQUEST_RECEIVED,
    PTTG_AUDIT_HISTORY_CORRELATION_IDS_RESPONSE_SUCCESS,
    PTTG_AUDIT_ARCHIVE_FAILURE,
    PTTG_AUDIT_ARCHIVE_RESULT_REQUEST_RECEIVED,
    PTTG_AUDIT_ARCHIVE_RESULT_RESPONSE_SUCCESS,
    PTTG_AUDIT_ARCHIVE_NOT_READY_FOR_NINO,
    PTTG_AUDIT_GET_ARCHIVED_RESULTS_REQUEST_RECEIVED,
    PTTG_AUDIT_GET_ARCHIVED_RESULTS_RESPONSE_SUCCESS;


    public static final String EVENT = "event_id";
}
