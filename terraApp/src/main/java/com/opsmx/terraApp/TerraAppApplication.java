package com.opsmx.terraApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

//@ComponentScan({"com.opsmx.terraApp.service","com.opsmx.terraApp.controller"})
@SpringBootApplication
public class TerraAppApplication {


	public static void main(String[] args) {
		SpringApplication.run(TerraAppApplication.class, args);
	}

}

