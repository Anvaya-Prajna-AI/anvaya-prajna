package ai.anvaya.prajna.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI anvayaPrajnaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Anvaya-Prajna Explanation Engine API")
                        .description("Educational reasoning & explanation engine — transforms assessment questions into structured Explanation IR")
                        .version("v1.0")
                        .contact(new Contact().name("Anvaya-Prajna Team").email("dev@anvaya.ai").url("https://github.com/Anvaya-Prajna-AI/anvaya-prajna"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList("HeaderAuth").addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("HeaderAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-User-Id")
                                .description("Header-based identity for internal gateway integration"))
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer token authentication for production OAuth2/OIDC")));
    }
}
