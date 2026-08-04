package com.chrizlove.vortexpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class VortexPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(VortexPayApplication.class, args);
    }

}
