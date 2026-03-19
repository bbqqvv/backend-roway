package org.bbqqvv.backendecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync  // Enables Spring's @Async processing
public class BackendECommerceApplication {

    public static void main(String[]         args) {
        SpringApplication.run(BackendECommerceApplication.class, args);
    }

}
