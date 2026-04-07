package senac.tsi.starwars.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SwapiPersonDto {
    private String name;
    private String gender;
    private String height;
    private String mass;
    private String homeworld; // URL do planeta natal

    @JsonProperty("birth_year")
    private String birthYear;

    // Lista de URLs de espécies (um personagem pode ter mais de uma)
    private List<String> species = new ArrayList<>();

    // Lista de URLs de naves pilotadas pelo personagem
    private List<String> starships = new ArrayList<>();

    private String url;
}
