package senac.tsi.starwars.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que espelha o JSON retornado pela SWAPI para planetas.
 * @JsonIgnoreProperties(ignoreUnknown = true) ignora campos extras da API.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SwapiPlanetDto {
    private String name;
    private String climate;
    private String terrain;
    private String population;
    private String diameter;
    private String url;
}
