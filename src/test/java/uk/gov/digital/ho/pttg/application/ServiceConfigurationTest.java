package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceConfigurationTest {

    private final ServiceConfiguration serviceConfiguration = new ServiceConfiguration(new ObjectMapper());

    @Test
    public void shouldCreateUTCClock() {
        Clock clock = serviceConfiguration.clock();

        assertThat(clock.getZone().getId()).isEqualTo("UTC");
    }
}