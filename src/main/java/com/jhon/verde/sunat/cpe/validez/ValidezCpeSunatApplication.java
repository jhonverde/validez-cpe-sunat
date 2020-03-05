package com.jhon.verde.sunat.cpe.validez;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ValidezCpeSunatApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValidezCpeSunatApplication.class, args);
    }

}
