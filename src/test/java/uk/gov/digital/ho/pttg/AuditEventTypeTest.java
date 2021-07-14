package uk.gov.digital.ho.pttg;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.AuditEventType.*;

public class AuditEventTypeTest {

    @Test
    public void shouldBeKnowItems() {
        assertThat(AuditEventType.values().length).isEqualTo(18);
    }

    @Test
    public void shouldBeAlertable() {
        assertThat(INCOME_PROVING_FINANCIAL_STATUS_REQUEST.isAlertable()).isTrue();
        assertThat(INCOME_PROVING_FINANCIAL_STATUS_RESPONSE.isAlertable()).isTrue();

        assertThat(INCOME_PROVING_INCOME_CHECK_REQUEST.isAlertable()).isTrue();
        assertThat(INCOME_PROVING_INCOME_CHECK_RESPONSE.isAlertable()).isTrue();
    }

    @Test
    public void shouldNotBeAlertable() {

        assertThat(HMRC_INCOME_REQUEST.isAlertable()).isFalse();
        assertThat(HMRC_ACCESS_CODE_REQUEST.isAlertable()).isFalse();

        assertThat(DWP_BENEFIT_REQUEST.isAlertable()).isFalse();

        assertThat(DWP_NINO_VALIDATE_REQUEST.isAlertable()).isFalse();
        assertThat(DWP_NINO_VALIDATE_DATA_MANIPULATION.isAlertable()).isFalse();
        assertThat(DWP_NINO_TRACE_ALLOCATE_REQUEST.isAlertable()).isFalse();
        assertThat(DWP_NINO_TRACE_ALLOCATE_DATA_MANIPULATION.isAlertable()).isFalse();

        assertThat(RESIDENCE_PROVING_INCOME_REQUEST.isAlertable()).isFalse();
        assertThat(RESIDENCE_PROVING_INCOME_RESPONSE.isAlertable()).isFalse();
        assertThat(RESIDENCE_PROVING_BENEFITS_REQUEST.isAlertable()).isFalse();
        assertThat(RESIDENCE_PROVING_BENEFITS_RESPONSE.isAlertable()).isFalse();

        assertThat(SEC_UI_RESULT_DOWNLOAD_REQUEST.isAlertable()).isFalse();

        assertThat(ARCHIVED_RESULTS.isAlertable()).isFalse();

        assertThat(IPS_STATISTICS.isAlertable()).isFalse();
    }
}
