package senac.tsi.starwars.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        String description = """
                API RESTful que disponibiliza o catalogo completo do universo Star Wars \
                com operacoes CRUD para filmes, personagens, planetas, especies e naves estelares. \
                Os dados foram extraidos da SWAPI e organizados em um modelo relacional com \
                respostas paginadas e navegacao hipermidia (HATEOAS).

                Leituras (GET) sao publicas. Escritas (POST, PUT, DELETE) exigem o header X-API-Key — \
                gere uma chave em POST /api/auth/keys. Limite de 20 requisicoes/minuto por IP. \
                Para evitar duplicidade em POST, envie X-Idempotency-Key. \
                Para formato alternativo de filmes, envie X-API-Version: 2.""";

        return new OpenAPI()
                .info(new Info()
                        .title("Star Wars API")
                        .version("1.0.0")
                        .description(description)
                        .contact(new Contact()
                                .name("Projeto Academico — SENAC TSI")
                                .url("https://github.com/zimmer")))
                .components(new Components()
                        .addSecuritySchemes("apiKey", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("Chave de API necessaria para operacoes de escrita. "
                                        + "Gere em POST /api/auth/keys e cole aqui.")))
                .addSecurityItem(new SecurityRequirement().addList("apiKey"))
                .addTagsItem(new Tag().name("Autenticacao"))
                .addTagsItem(new Tag().name("Films V2"))
                .addTagsItem(new Tag().name("Films"))
                .addTagsItem(new Tag().name("People"))
                .addTagsItem(new Tag().name("Planets"))
                .addTagsItem(new Tag().name("Species"))
                .addTagsItem(new Tag().name("Starships"));
    }
}
