package com.github.tgiachi.cubemediaserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Properties;


@SpringBootApplication
public class CubemediaserverApplication {

	public static void main(String[] args) {


		SpringApplication app = new SpringApplication(CubemediaserverApplication.class);

		app.run(args);


	}
}


