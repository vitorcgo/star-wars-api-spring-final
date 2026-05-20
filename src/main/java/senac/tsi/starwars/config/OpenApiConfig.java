package senac.tsi.starwars.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Star Wars API")
                        .version("1.0.0")
                        .description("API RESTful do universo Star Wars com dados da SWAPI. "
                                + "Suporta paginação, HATEOAS, autenticação por API Key, "
                                + "rate limiting, idempotência e versionamento por header.")
                        .contact(new Contact()
                                .name("Projeto Acadêmico — SENAC TSI")
                                .url("https://github.com/zimmer")))
                .components(new Components()
                        .addSecuritySchemes("apiKey", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("Chave de API. Gere em POST /api/auth/keys e cole aqui.")))
                .addSecurityItem(new SecurityRequirement().addList("apiKey"));
    }
}
