package com.climbe.api_climbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApiClimbeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiClimbeApplication.class, args);
	}

}
