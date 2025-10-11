package com.quokka.jobmate_connect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobmateConnectApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobmateConnectApplication.class, args);
	}

}
