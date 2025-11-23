package com.app.order.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class OpenApiConfig {
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Productos")
                        .version("1.0")
                        .description("API's de gestion productos levelUp")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            List<String> publicEndpoints = Arrays.asList(
                    "/api/v1/productos/list",
                    "/api/v1/productos/estado/activos",
                    "/api/v1/productos/buscar/nombre",
                    "/api/v1/productos/buscar/precio"
            );

            openApi.getPaths().forEach((path, pathItem) -> {
                boolean isPublicEndpoint = publicEndpoints.stream()
                        .anyMatch(publicPath -> path.matches(publicPath + ".*"));

                if (!isPublicEndpoint) {
                    applySecurityToPathItem(pathItem);
                }
            });
        };
    }

    private void applySecurityToPathItem(PathItem pathItem) {
        if (pathItem.getGet() != null) {
            applySecurityToOperation(pathItem.getGet());
        }
        if (pathItem.getPost() != null) {
            applySecurityToOperation(pathItem.getPost());
        }
        if (pathItem.getPut() != null) {
            applySecurityToOperation(pathItem.getPut());
        }
        if (pathItem.getDelete() != null) {
            applySecurityToOperation(pathItem.getDelete());
        }
        if (pathItem.getPatch() != null) {
            applySecurityToOperation(pathItem.getPatch());
        }
    }

    private void applySecurityToOperation(Operation operation) {
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(SECURITY_SCHEME_NAME);
        operation.addSecurityItem(securityRequirement);
    }
}