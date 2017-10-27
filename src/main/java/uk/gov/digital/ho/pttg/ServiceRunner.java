package uk.gov.digital.ho.pttg;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport
public class ServiceRunner {

	public static void main(String[] args) {
		SpringApplication.run(ServiceRunner.class, args);
	}
}
