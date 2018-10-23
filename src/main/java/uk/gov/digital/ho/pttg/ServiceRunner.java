package uk.gov.digital.ho.pttg;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@SpringBootApplication
@EnableSpringDataWebSupport
@Slf4j
public class ServiceRunner {

	public static void main(String[] args) {
		SpringApplication.run(ServiceRunner.class, args);
		log.info("Audit service started", value(EVENT, PTTG_AUDIT_SERVICE_STARTED));
	}
}
