package uk.gov.digital.ho.pttg;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.AuditEventType.*;

public class AuditEventTypeTest {

    @Test
    public void shouldBeKnowItems() {
        assertThat(AuditEventType.values().length).isEqualTo(7);
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
        assertThat(RESIDENCE_PROVING_EMPLOYMENT_STATUS_REQUEST.isAlertable()).isFalse();
        assertThat(RESIDENCE_PROVING_EMPLOYMENT_STATUS_RESPONSE.isAlertable()).isFalse();
    }
}