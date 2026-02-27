package dev.prospectos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ProspectosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProspectosApplication.class, args);
    }

}
