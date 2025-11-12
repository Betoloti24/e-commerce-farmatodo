package com.farmatodo.apigetway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ApigetwayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApigetwayApplication.class, args);
	}

}
