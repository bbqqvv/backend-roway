
package org.bbqqvv.backendecommerce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("ROWAY E-Commerce API")
                        .version("1.0.0")
                        .description("Hệ thống API backend cho ứng dụng E-Commerce ROWAY. Hỗ trợ xác thực JWT và quản lý sản phẩm, đơn hàng chuyên nghiệp.")
                        .contact(new io.swagger.v3.oas.models.info.Contact()
                                .name("ROWAY Support")
                                .email("support@roway.com")
                                .url("https://roway.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org"))
                )
                .addServersItem(new io.swagger.v3.oas.models.servers.Server().url("http://localhost:8080").description("Local Development Server"))
                .addServersItem(new io.swagger.v3.oas.models.servers.Server().url("https://api.roway.com").description("Production Server"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components().addSecuritySchemes(securitySchemeName,
                        new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
