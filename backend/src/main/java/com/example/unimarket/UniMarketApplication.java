package com.example.unimarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UniMarketApplication {

	public static void main(String[] args) {
		SpringApplication.run(UniMarketApplication.class, args);
	}

}
