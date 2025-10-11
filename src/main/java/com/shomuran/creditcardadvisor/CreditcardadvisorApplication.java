package com.shomuran.creditcardadvisor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CreditcardadvisorApplication {

	public static void main(String[] args) {

		SpringApplication.run(CreditcardadvisorApplication.class, args);
	}

}
