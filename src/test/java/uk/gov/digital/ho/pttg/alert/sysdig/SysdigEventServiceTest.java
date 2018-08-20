package uk.gov.digital.ho.pttg.alert.sysdig;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.alert.IndividualVolumeUsage;
import uk.gov.digital.ho.pttg.alert.MatchingFailureUsage;
import uk.gov.digital.ho.pttg.alert.TimeOfRequestUsage;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class SysdigEventServiceTest {

    private static final String SYSDIG_ENDPOINT = "some sysdig endpoint";
    private static final String SYSDIG_ACCESS_TOKEN = "some sysdig access token";
    private static final String NAMESPACE = "some namespace";

    @Mock private RestTemplate mockRestTemplate;

    @Captor private ArgumentCaptor<HttpEntity> captorHttpEntity;

    private SysdigEventService sysdigEventService;

    @Before
    public void setup() {
        sysdigEventService = new SysdigEventService(mockRestTemplate,
                                                    SYSDIG_ENDPOINT,
                                                    SYSDIG_ACCESS_TOKEN,
                                                    NAMESPACE);
    }

    @Test
    public void shouldUseCollaboratorsOf_sendUsersExceedUsageThresholdEvent() {
        IndividualVolumeUsage usage = new IndividualVolumeUsage(Collections.emptyMap());

        sysdigEventService.sendUsersExceedUsageThresholdEvent(usage);

        verify(mockRestTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    public void shouldPostHttEntityToSysdig_sendUsersExceedUsageThresholdEvent() {
        IndividualVolumeUsage usage = new IndividualVolumeUsage(Collections.emptyMap());

        sysdigEventService.sendUsersExceedUsageThresholdEvent(usage);

        verify(mockRestTemplate).exchange(eq("some sysdig endpoint"), eq(HttpMethod.POST), captorHttpEntity.capture(), eq(Void.class));

        checkHeaders(captorHttpEntity.getValue().getHeaders());

        checkMessage((Message) captorHttpEntity.getValue().getBody(),
                "Proving Things, Income Proving, Excessive Usage",
                String.format("Excessive usage detected; %s", usage.getCountsByUser()));
    }

    @Test
    public void shouldPostHttEntityToSysdig_sendRequestsOutsideHoursEvent() {
        TimeOfRequestUsage usage = new TimeOfRequestUsage(0);

        sysdigEventService.sendRequestsOutsideHoursEvent(usage);

        verify(mockRestTemplate).exchange(eq("some sysdig endpoint"), eq(HttpMethod.POST), captorHttpEntity.capture(), eq(Void.class));

        checkHeaders(captorHttpEntity.getValue().getHeaders());

        checkMessage((Message) captorHttpEntity.getValue().getBody(),
                "Proving Things, Income Proving, Out of hours activity",
                String.format("Activity detected outside usual hours; %d requests made", usage.getRequestCount()));
    }

    @Test
    public void shouldPostHttEntityToSysdig_sendMatchingFailuresExceedThresholdEvent() {
        MatchingFailureUsage usage = new MatchingFailureUsage(0);

        sysdigEventService.sendMatchingFailuresExceedThresholdEvent(usage);

        verify(mockRestTemplate).exchange(eq("some sysdig endpoint"), eq(HttpMethod.POST), captorHttpEntity.capture(), eq(Void.class));

        checkHeaders(captorHttpEntity.getValue().getHeaders());

        checkMessage((Message) captorHttpEntity.getValue().getBody(),
                "Proving Things, Income Proving, Excessive match failures",
                String.format("Excessive match failures detected; %d match failures", usage.getCountOfFailures()));
    }

    private void checkHeaders(HttpHeaders headers) {
        assertThat(headers.containsKey(HttpHeaders.CONTENT_TYPE)).isTrue();
        assertThat(headers.get(HttpHeaders.CONTENT_TYPE).get(0)).isEqualTo(APPLICATION_JSON_VALUE);
        assertThat(headers.containsKey(HttpHeaders.AUTHORIZATION)).isTrue();
        assertThat(headers.get(HttpHeaders.AUTHORIZATION).get(0)).isEqualTo(String.format("Bearer %s", SYSDIG_ACCESS_TOKEN));
    }

    private void checkMessage(Message message, String expectedEventName, String expectedEventDescription) {
        Event event = message.getEvent();
        assertThat(event.getName()).isEqualTo(expectedEventName);
        assertThat(event.getDescription()).isEqualTo(expectedEventDescription);
        assertThat(event.getSeverity()).isEqualTo("6");
        assertThat(event.getFilter()).isEqualTo(String.format("kubernetes.namespace.name='%s'", NAMESPACE));
        assertThat(event.getTags()).isEqualTo(ImmutableMap.of("source", String.format("PT-IP - %s", NAMESPACE),
                                                                "project", "PTTG"));
    }
}