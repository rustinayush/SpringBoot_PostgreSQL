package com.example.MultiTenancy_Cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication
public class MultiTenancyCacheApplication {

	public static void main(String[] args) {
		SpringApplication.run(MultiTenancyCacheApplication.class, args);
		System.out.println("Server is started!");
	}
}


