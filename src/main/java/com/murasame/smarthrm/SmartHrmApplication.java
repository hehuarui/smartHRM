package com.murasame.smarthrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;

@SpringBootApplication
public class SmartHrmApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartHrmApplication.class, args);
		LocalDateTime now = LocalDateTime.now().withNano(0);
		System.out.println(now + " : Smart Human Resource Management Started");
	}

}
