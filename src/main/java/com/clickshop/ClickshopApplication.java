package com.clickshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication(scanBasePackages = "com.clickshop")
public class ClickshopApplication {

	public static void main(String[] args) {
		
		ConfigurableApplicationContext context = SpringApplication.run(ClickshopApplication.class, args);
		Environment env = context.getEnvironment();
		System.out.println("Clickshop Backend started at port number " + env.getProperty("server.port"));

	}

}
