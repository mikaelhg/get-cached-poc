package io.mikael.poc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class GetCachedApplication {

	public static void main(String[] args) {
		SpringApplication.run(GetCachedApplication.class, args);
	}

}
