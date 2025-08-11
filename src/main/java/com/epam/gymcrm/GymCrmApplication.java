package com.epam.gymcrm;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@OpenAPIDefinition(
        info = @Info(
                title = "OpenRemote Asset Service REST API Documentation",
                description = "REST API documentation for Asset operations (Create, Get, Update, Delete)",
                version = "v1",
                contact = @Contact(
                        name = "Tutku Ince",
                        email = "tutku_ince@epam.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "Swagger UI",
                url = "http://localhost:8080/swagger-ui/index.html"
        )
)
@SpringBootApplication
public class GymCrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymCrmApplication.class);
    }
}
