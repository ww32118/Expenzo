package com.Expenzo_Backend.Expenzo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ExpenzoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpenzoApplication.class, args);
	}

}
