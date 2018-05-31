package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceConfigurationTest {

    private ServiceConfiguration serviceConfiguration;

    @Mock
    private RestTemplateBuilder mockRestTemplateBuilder;

    @Mock
    private RestTemplate mockRestTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        when(mockRestTemplateBuilder.setReadTimeout(anyInt())).thenReturn(mockRestTemplateBuilder);
        when(mockRestTemplateBuilder.setConnectTimeout(anyInt())).thenReturn(mockRestTemplateBuilder);

        when(mockRestTemplateBuilder.build()).thenReturn(mockRestTemplate);
    }

    @Test
    public void shouldSetTimeoutsOnRestTemplate() {
        // given
        int readTimeout = 1234;
        int connectTimeout = 4321;
        serviceConfiguration = new ServiceConfiguration(objectMapper, readTimeout, connectTimeout);

        // when
        RestTemplate restTemplate = serviceConfiguration.restTemplate(mockRestTemplateBuilder);

        // then
        verify(mockRestTemplateBuilder).setReadTimeout(readTimeout);
        verify(mockRestTemplateBuilder).setConnectTimeout(connectTimeout);

        assertThat(restTemplate).isEqualTo(mockRestTemplate);
    }

    @Test
    public void shouldCreateUTCClock() {
        // given
        serviceConfiguration = new ServiceConfiguration(objectMapper, 0, 0);

        // when
        Clock clock = serviceConfiguration.clock();

        // then
        assertThat(clock.getZone().getId()).isEqualTo("UTC");
    }
}