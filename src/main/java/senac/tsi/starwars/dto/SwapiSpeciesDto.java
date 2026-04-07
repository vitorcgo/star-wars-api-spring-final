package senac.tsi.starwars.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SwapiSpeciesDto {
    private String name;
    private String classification;
    private String language;
    private String homeworld; // URL do planeta natal

    @JsonProperty("average_height")
    private String averageHeight;

    @JsonProperty("average_lifespan")
    private String averageLifespan;

    private String url;
}
