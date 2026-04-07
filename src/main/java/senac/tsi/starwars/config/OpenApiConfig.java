package senac.tsi.starwars.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
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
                                """));
    }
}
