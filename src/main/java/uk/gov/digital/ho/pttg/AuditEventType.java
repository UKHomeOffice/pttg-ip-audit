package uk.gov.digital.ho.pttg;

public enum AuditEventType {

    HMRC_INCOME_REQUEST,
    HMRC_ACCESS_CODE_REQUEST,

    INCOME_PROVING_FINANCIAL_STATUS_REQUEST(true),
    INCOME_PROVING_FINANCIAL_STATUS_RESPONSE(true),

    INCOME_PROVING_INCOME_CHECK_REQUEST(true),
    INCOME_PROVING_INCOME_CHECK_RESPONSE(true),

    RESIDENCE_PROVING_INCOME_REQUEST,
    RESIDENCE_PROVING_INCOME_RESPONSE,
    RESIDENCE_PROVING_BENEFITS_REQUEST,
    RESIDENCE_PROVING_BENEFITS_RESPONSE,

    DWP_BENEFIT_REQUEST,
    DWP_NINO_VALIDATE_REQUEST,
    DWP_NINO_VALIDATE_DATA_MANIPULATION,
    DWP_NINO_TRACE_ALLOCATE_REQUEST,
    DWP_NINO_TRACE_ALLOCATE_DATA_MANIPULATION,

    ARCHIVED_RESULTS,

    IPS_STATISTICS;

    private boolean alertable;

    AuditEventType(boolean alertable) {
        this.alertable = alertable;
    }

    AuditEventType() {
        this(false);
    }

    public boolean isAlertable() {
        return this.alertable;
    }
}
