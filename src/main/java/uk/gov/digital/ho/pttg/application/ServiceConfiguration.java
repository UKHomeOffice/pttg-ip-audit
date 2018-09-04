package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ServiceConfiguration {

    private final int restTemplateReadTimeoutInMillis;
    private final int restTemplateConnectTimeoutInMillis;

    @Autowired
    public ServiceConfiguration(ObjectMapper objectMapper,
                                @Value("${resttemplate.timeout.read:30000}") int restTemplateReadTimeoutInMillis,
                                @Value("${resttemplate.timeout.connect:30000}") int restTemplateConnectTimeoutInMillis) {
        this.restTemplateReadTimeoutInMillis = restTemplateReadTimeoutInMillis;
        this.restTemplateConnectTimeoutInMillis = restTemplateConnectTimeoutInMillis;
        initialiseObjectMapper(objectMapper);
    }

    private static void initialiseObjectMapper(final ObjectMapper m) {
        m.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

        m.registerModule(new JavaTimeModule());
        m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        m.enable(SerializationFeature.INDENT_OUTPUT);
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .setReadTimeout(restTemplateReadTimeoutInMillis)
                .setConnectTimeout(restTemplateConnectTimeoutInMillis)
                .build();
    }

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("UTC"));
    }

    @Bean
    PageableHandlerMethodArgumentResolverCustomizer maxPageSizeCustomizer() {
        return p -> p.setMaxPageSize(100_000);
    }

}