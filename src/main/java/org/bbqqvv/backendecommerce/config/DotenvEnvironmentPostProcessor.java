package org.bbqqvv.backendecommerce.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            Map<String, Object> dotEnvProps = new HashMap<>();
            dotenv.entries().forEach(entry -> dotEnvProps.put(entry.getKey(), entry.getValue()));

            if (!dotEnvProps.isEmpty()) {
                environment.getPropertySources().addLast(new MapPropertySource("dotenvProperties", dotEnvProps));
            }
        } catch (Exception e) {
            // Ignore - we'll rely on real env variables if .env fails
        }
    }
}
