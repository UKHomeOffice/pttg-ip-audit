package uk.gov.digital.ho.pttg.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.digital.ho.pttg.api.RequestData;

@Configuration
public class MvcConfigurer implements WebMvcConfigurer {

    @Bean
    public RequestData createRequestData() {
        return new RequestData();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(createRequestData());
    }
}
