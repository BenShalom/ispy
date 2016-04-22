package com.sambram.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class IspyApplication {

	public static void main(String[] args) {
		SpringApplication.run(IspyApplication.class, args);
	}
}
