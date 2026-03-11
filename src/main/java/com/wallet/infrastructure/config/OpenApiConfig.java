package com.wallet.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI walletServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Wallet Service API")
                        .description("Digital Wallet Service with Clean Architecture - REST API for managing wallets, transactions, and users")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Wallet Service Team")
                                .email("support@wallet.service")));
    }
}
