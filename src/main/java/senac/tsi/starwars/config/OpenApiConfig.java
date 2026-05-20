package senac.tsi.starwars.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
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
                        .title("Star Wars SWAPI - API REST")
                        .version("1.0.0")
                        .description("""
                                API RESTful que consome dados da SWAPI (https://swapi.info/)
                                e os expõe com suporte a paginação, HATEOAS e documentação Swagger.
                                Projeto Acadêmico — Spring Boot 4 + Java 25.

                                ## Autenticação
                                Endpoints de escrita (POST, PUT, DELETE) exigem header `X-API-Key`.
                                Gere sua chave em `POST /api/auth/keys`.

                                ## Rate Limiting
                                Limite de 20 requisições por minuto por IP.
                                Headers de resposta: `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `Retry-After`.

                                ## Idempotência
                                Envie header `X-Idempotency-Key` em requisições POST para evitar duplicação.

                                ## Versionamento
                                Use header `X-API-Version: 2` para acessar formato resumido dos endpoints de filmes.
                                """))
                .components(new Components()
                        .addSecuritySchemes("apiKey", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("Chave de API para autenticação. Gere em POST /api/auth/keys")))
                .addSecurityItem(new SecurityRequirement().addList("apiKey"));
    }
}
