package com.jhon.verde.cpe.sunat.validez;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableAsync
public class ValidezCpeSunatApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValidezCpeSunatApplication.class, args);
    }

}
