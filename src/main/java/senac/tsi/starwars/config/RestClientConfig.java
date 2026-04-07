package senac.tsi.starwars.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient swapiRestClient() {
        return RestClient.builder()
                .baseUrl("https://swapi.info/api")
                .defaultHeader("User-Agent", "StarWars-SWAPI-Consumer/1.0")
                .build();
    }
}
