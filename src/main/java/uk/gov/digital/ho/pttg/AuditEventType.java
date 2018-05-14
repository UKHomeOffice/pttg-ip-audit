package uk.gov.digital.ho.pttg;

public enum AuditEventType {

    HMRC_INCOME_REQUEST(false),
    HMRC_ACCESS_CODE_REQUEST(false),

    INCOME_PROVING_FINANCIAL_STATUS_REQUEST(true),
    INCOME_PROVING_FINANCIAL_STATUS_RESPONSE(true),

    INCOME_PROVING_INCOME_CHECK_REQUEST(true),
    INCOME_PROVING_INCOME_CHECK_RESPONSE(true),

    RESIDENCE_PROVING_INCOME_REQUEST(false),
    RESIDENCE_PROVING_INCOME_RESPONSE(false),
    RESIDENCE_PROVING_BENEFITS_REQUEST(false),
    RESIDENCE_PROVING_BENEFITS_RESPONSE(false),

    DWP_BENEFIT_REQUEST(false);

    private boolean alertable;

    AuditEventType(boolean alertable) {
        this.alertable = alertable;
    }

    public boolean isAlertable() {
        return this.alertable;
    }
}
