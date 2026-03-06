package br.com.notafacil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@org.springframework.scheduling.annotation.EnableScheduling
@SpringBootApplication
@EnableCaching
public class NotaFacilApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotaFacilApplication.class, args);
    }
}