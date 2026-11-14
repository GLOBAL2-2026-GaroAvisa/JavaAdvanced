package br.com.GaroAvisa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GaroAvisaApplication {

	public static void main(String[] args) {
		SpringApplication.run(GaroAvisaApplication.class, args);
	}

}
